import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { getAuthOptions } from '@/app/api/auth/[...nextauth]/auth-options';

export const dynamic = 'force-dynamic';

export default async function Home() {
  const session = await getServerSession(getAuthOptions());

  if (session) {
    redirect('/dashboard');
  } else {
    redirect('/login');
  }
}
