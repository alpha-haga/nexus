import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { AppLayout, ComingSoon } from '@/modules/core';

export default async function GojoCeremoniesPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon title="施行連携" description="施行連携機能を実装予定" />
    </AppLayout>
  );
}