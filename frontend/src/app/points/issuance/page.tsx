import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { getAuthOptions } from '@/app/api/auth/[...nextauth]/auth-options';
import { AppLayout, ComingSoon } from '@/modules/core';

export const dynamic = 'force-dynamic';

export default async function PointsIssuancePage() {
  const session = await getServerSession(getAuthOptions());

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon title="発行（単独 / 一括）" description="ポイント発行機能を実装予定" />
    </AppLayout>
  );
}