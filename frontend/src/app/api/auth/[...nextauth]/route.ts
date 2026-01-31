import NextAuth, { AuthOptions } from 'next-auth';
import KeycloakProvider from 'next-auth/providers/keycloak';

// Keycloak設定の必須チェック（本番運用安全性のため）
if (!process.env.KEYCLOAK_CLIENT_ID || !process.env.KEYCLOAK_CLIENT_SECRET || !process.env.KEYCLOAK_ISSUER) {
  throw new Error(
    'Keycloak設定が不足しています。KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET, KEYCLOAK_ISSUER を設定してください。'
  );
}

export const authOptions: AuthOptions = {
  providers: [
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: process.env.KEYCLOAK_ISSUER!,
    }),
  ],
  callbacks: {
    async jwt({ token, account, profile }) {
      if (account && account.provider === 'keycloak' && profile) {
        token.accessToken = account.access_token;
        token.refreshToken = account.refresh_token;
        token.accessTokenExpires = account.expires_at ? account.expires_at * 1000 : undefined;
        token.id = profile.sub ?? '';
        token.employeeId =
          ((profile as Record<string, unknown>).employeeId as string) ??
          profile.sub ??
          '';
        token.corporationId = (profile as Record<string, unknown>)
          .corporationId as string;
        token.corporationName = (profile as Record<string, unknown>)
          .corporationName as string;
        token.error = token.refreshToken ? undefined : 'NoRefreshToken';
      }

      // Token refresh 判定（account に依存しない）
      if (token.refreshToken && token.accessTokenExpires) {
        const now = Date.now();
        const expiresAt = token.accessTokenExpires;
        const bufferTime = 60 * 1000; // 1分前

        // 期限切れ1分前を閾値として refresh を実行
        if (now >= expiresAt - bufferTime) {
          try {
            const issuer = process.env.KEYCLOAK_ISSUER;
            if (!issuer) {
              token.error = 'RefreshAccessTokenError';
              return token;
            }

            const response = await fetch(`${issuer}/protocol/openid-connect/token`, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
              },
              body: new URLSearchParams({
                grant_type: 'refresh_token',
                refresh_token: token.refreshToken,
                client_id: process.env.KEYCLOAK_CLIENT_ID!,
                client_secret: process.env.KEYCLOAK_CLIENT_SECRET!,
              }),
            });

            const refreshedTokens = await response.json();

            if (response.ok && refreshedTokens.access_token) {
              token.accessToken = refreshedTokens.access_token;
              token.refreshToken = refreshedTokens.refresh_token ?? token.refreshToken;
              token.accessTokenExpires =
                Date.now() + (refreshedTokens.expires_in * 1000);
              token.error = undefined;
            } else {
              // refresh 失敗時: token をクリアせず error のみセット（状態を隠さない）
              token.error = 'RefreshAccessTokenError';
            }
          } catch (error) {
            console.error('Token refresh failed:', error);
            // refresh 失敗時: token をクリアせず error のみセット（状態を隠さない）
            token.error = 'RefreshAccessTokenError';
          }
        }
      }

      return token;
    },
    async session({ session, token }) {
      session.user = {
        id: token.id,
        employeeId: token.employeeId,
        name: token.name ?? '',
        email: token.email ?? '',
        corporationId: token.corporationId,
        corporationName: token.corporationName,
      };
      session.accessToken = token.accessToken;
      session.error = token.error;
      return session;
    },
  },
  pages: {
    signIn: '/login',
  },
  session: {
    strategy: 'jwt',
  },
};

const handler = NextAuth(authOptions);

export { handler as GET, handler as POST };