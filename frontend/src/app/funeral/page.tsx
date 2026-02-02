'use client';

import { useSession } from 'next-auth/react';
import { AppLayout } from '@/modules/core';
import { useScreenPermission } from '@/modules/core/auth/useScreenPermission';
import { Forbidden } from '@/modules/core/components/errors/Forbidden';

export default function FuneralPage() {
  const { data: session, status } = useSession();
  const permission = useScreenPermission('funeral');

  // 認証中は一旦表示（ローディング状態）
  if (status === 'loading') {
    return (
      <AppLayout>
        <div className="max-w-6xl">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-900">葬祭</h1>
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
        screenName="葬祭"
        reason={permission.reason}
      />
    );
  }

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-gray-900">葬祭</h1>
          <p className="text-sm text-gray-500 mt-1">nexus-funeral - 葬祭案件の管理</p>
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
                d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
              />
            </svg>
            <p className="text-gray-500 text-sm">この画面は準備中です</p>
            <p className="text-gray-400 text-xs mt-2">
              葬祭案件の受付・施行管理・工程管理機能を実装予定
            </p>
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
