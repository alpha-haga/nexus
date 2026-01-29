// frontend/src/app/group/contracts/page.tsx（更新）

import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { AppLayout } from '@/modules/core';
import { GroupContractsList } from '@/modules/group/components/GroupContractsList';

export default async function GroupContractsPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <div className="max-w-7xl">
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-gray-900">法人横断契約一覧</h1>
          <p className="text-sm text-gray-500 mt-1">
            統合DBの契約一覧（検索条件を指定して検索してください）
          </p>
        </div>

        <GroupContractsList />
      </div>
    </AppLayout>
  );
}