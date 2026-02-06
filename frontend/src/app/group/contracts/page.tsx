'use client';

import { useSession } from 'next-auth/react';
import { AppLayout } from '@/modules/core';
import { GroupContractsList } from '@/modules/group/components/GroupContractsList';
import { useScreenPermission } from '@/modules/core/auth/useScreenPermission';
import { Forbidden } from '@/modules/core/components/errors/Forbidden';

export default function GroupContractsPage() {
  const { data: session, status } = useSession();
  const permission = useScreenPermission('groupContracts');

  // 認証中は一旦表示（ローディング状態）
  if (status === 'loading') {
    return (
      <AppLayout>
        <div className="w-full max-w-none min-w-0">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">法人横断契約一覧</h1>
            <p className="text-sm text-gray-500 mt-1">読み込み中...</p>
          </div>
        </div>
      </AppLayout>
    );
  }

  // 未認証の場合はログイン画面にリダイレクト（NextAuth が処理）
  if (!session) {
    return null;
  }

  // 権限がない場合は 403 表示
  if (!permission.canView) {
    return (
      <Forbidden
        screenName="法人横断契約一覧"
        reason={permission.reason}
      />
    );
  }

  return (
    <AppLayout>
      <div className="w-full max-w-none min-w-0">
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
