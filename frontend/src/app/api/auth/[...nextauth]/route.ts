import NextAuth, { AuthOptions } from 'next-auth';
import KeycloakProvider from 'next-auth/providers/keycloak';
import CredentialsProvider from 'next-auth/providers/credentials';

const isDevelopment = process.env.NODE_ENV === 'development';

export const authOptions: AuthOptions = {
  providers: [
    // 開発用ダミー認証（将来 Keycloak に切り替え）
    CredentialsProvider({
      id: 'credentials',
      name: 'Credentials',
      credentials: {
        userId: { label: 'ユーザーID', type: 'text' },
        password: { label: 'パスワード', type: 'password' },
      },
      async authorize(credentials) {
        // ダミー認証: どのID/パスワードでもログイン可能
        // 将来は Keycloak の Resource Owner Password Credentials Grant に置き換え可能
        if (credentials?.userId && credentials?.password) {
          return {
            id: credentials.userId,
            employeeId: credentials.userId,
            name: `${credentials.userId}`,
            email: `${credentials.userId}@example.com`,
            corporationId: 'corp-001',
            corporationName: 'サンプル法人',
          };
        }
        return null;
      },
    }),
    // 本番用 Keycloak 認証
    ...(process.env.KEYCLOAK_CLIENT_ID
      ? [
          KeycloakProvider({
            clientId: process.env.KEYCLOAK_CLIENT_ID,
            clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
            issuer: process.env.KEYCLOAK_ISSUER,
          }),
        ]
      : []),
  ],
  callbacks: {
    async jwt({ token, account, profile, user }) {
      if (account && account.provider === 'keycloak' && profile) {
        // Keycloak から取得したユーザー属性をトークンにマッピング
        token.accessToken = account.access_token;
        token.id = profile.sub ?? '';
        token.employeeId =
          ((profile as Record<string, unknown>).employeeId as string) ??
          profile.sub ??
          '';
        token.corporationId = (profile as Record<string, unknown>)
          .corporationId as string;
        token.corporationName = (profile as Record<string, unknown>)
          .corporationName as string;
      } else if (user) {
        // Credentials プロバイダからのユーザー情報
        token.id = user.id;
        token.employeeId = user.employeeId;
        token.name = user.name;
        token.email = user.email;
        token.corporationId = user.corporationId;
        token.corporationName = user.corporationName;
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
