import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { getAuthOptions } from '@/app/api/auth/[...nextauth]/auth-options';
import { AppLayout, ComingSoon } from '@/modules/core';

export const dynamic = 'force-dynamic';

export default async function PointsMonthlyClosePage() {
  const session = await getServerSession(getAuthOptions());

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon title="月次締め" description="ポイント月次締め機能を実装予定" />
    </AppLayout>
  );
}