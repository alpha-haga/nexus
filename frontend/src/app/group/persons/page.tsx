import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { getAuthOptions } from '@/app/api/auth/[...nextauth]/auth-options';
import { AppLayout, ComingSoon } from '@/modules/core';

export const dynamic = 'force-dynamic';

export default async function GroupPersonsPage() {
  const session = await getServerSession(getAuthOptions());

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <ComingSoon
        title="法人横断検索"
        description="法人横断検索機能を実装予定（Integration DB）"
      />
    </AppLayout>
  );
}