'use client';

import { useSession } from 'next-auth/react';
import { AppLayout } from '@/modules/core';
import { useScreenPermission } from '@/modules/core/auth/useScreenPermission';
import { Forbidden } from '@/modules/core/components/errors/Forbidden';

export default function GojoPage() {
  const { data: session, status } = useSession();
  const permission = useScreenPermission('gojo');

  // 認証中は一旦表示（ローディング状態）
  if (status === 'loading') {
    return (
      <AppLayout>
        <div className="max-w-6xl">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">互助会</h1>
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
        screenName="互助会"
        reason={permission.reason}
      />
    );
  }

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-gray-900">互助会</h1>
          <p className="text-sm text-gray-500 mt-1">nexus-gojo - 互助会契約の管理</p>
        </div>

        <div className="card">
          <div className="flex flex-col items-center justify-center py-12">
            <svg
              className="w-16 h-16 text-gray-300 mb-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            <p className="text-gray-500 text-sm">この画面は準備中です</p>
            <p className="text-gray-400 text-xs mt-2">
              互助会契約の一覧・検索・詳細表示機能を実装予定
            </p>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
