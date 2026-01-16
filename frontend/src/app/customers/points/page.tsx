import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { AppLayout, ComingSoon } from '@/modules/core';

export default async function CustomerPointsPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon title="ポイント参照" description="ポイント参照機能を実装予定（読み取り専用）" />
    </AppLayout>
  );
}