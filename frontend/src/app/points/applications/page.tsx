import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { AppLayout, ComingSoon } from '@/modules/core';

export default async function PointsApplicationsPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon title="カード申込" description="ポイントカード申込機能を実装予定" />
    </AppLayout>
  );
}