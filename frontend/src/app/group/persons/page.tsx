'use client';

import { useSession } from 'next-auth/react';
import { AppLayout, ComingSoon } from '@/modules/core';
import { useScreenPermission } from '@/modules/core/auth/useScreenPermission';
import { Forbidden } from '@/modules/core/components/errors/Forbidden';

export default function GroupPersonsPage() {
  const { data: session, status } = useSession();
  const permission = useScreenPermission('groupPersons');

  // 認証中は一旦表示（ローディング状態）
  if (status === 'loading') {
    return (
      <AppLayout>
        <div className="max-w-6xl">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">法人横断検索</h1>
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
        screenName="法人横断検索"
        reason={permission.reason}
      />
    );
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
