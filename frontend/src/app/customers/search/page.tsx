import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { AppLayout, ComingSoon } from '@/modules/core';

export default async function CustomerSearchPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon
        title="会員検索"
        description="会員検索機能を実装予定（Local / All Regions 切替）"
      />
    </AppLayout>
  );
}