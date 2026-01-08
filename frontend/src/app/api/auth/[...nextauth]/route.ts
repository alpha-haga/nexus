import NextAuth, { AuthOptions } from 'next-auth';
import KeycloakProvider from 'next-auth/providers/keycloak';

export const authOptions: AuthOptions = {
  providers: [
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: process.env.KEYCLOAK_ISSUER,
    }),
  ],
  callbacks: {
    async jwt({ token, account, profile }) {
      if (account && profile) {
        // Keycloak から取得したユーザー属性をトークンにマッピング
        token.accessToken = account.access_token;
        token.id = profile.sub ?? '';
        // Keycloak のカスタム属性（社員マスタと紐づく想定）
        token.employeeId = (profile as Record<string, unknown>).employeeId as string ?? profile.sub ?? '';
        token.corporationId = (profile as Record<string, unknown>).corporationId as string;
        token.corporationName = (profile as Record<string, unknown>).corporationName as string;
      }
      return token;
    },
    async session({ session, token }) {
      // JWT の情報をセッションに反映
      session.user = {
        id: token.id,
        employeeId: token.employeeId,
        name: session.user?.name ?? '',
        email: session.user?.email ?? '',
        corporationId: token.corporationId,
        corporationName: token.corporationName,
      };
      session.accessToken = token.accessToken;
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
