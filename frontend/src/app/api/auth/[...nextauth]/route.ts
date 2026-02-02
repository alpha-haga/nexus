import NextAuth from 'next-auth';
import { getAuthOptions } from './auth-options';

export async function GET(req: Request, context: { params: unknown }) {
  const handler = NextAuth(getAuthOptions());
  return handler(req, context);
}

export async function POST(req: Request, context: { params: unknown }) {
  const handler = NextAuth(getAuthOptions());
  return handler(req, context);
}
