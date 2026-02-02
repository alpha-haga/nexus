/**
 * 403 Forbidden エラー表示コンポーネント
 * 
 * 権限不足時に表示する共通 UI
 */

'use client';

interface ForbiddenProps {
  /** 対象画面名 */
  screenName: string;
  /** 拒否理由 */
  reason?: string;
}

export function Forbidden({ screenName, reason }: ForbiddenProps) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white rounded-lg shadow-md p-6">
        <div className="flex items-center justify-center mb-4">
          <div className="bg-red-100 rounded-full p-3">
            <svg
              className="w-8 h-8 text-red-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
        </div>

        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            権限がありません（403）
          </h1>
          <p className="text-gray-600 mb-4">
            <span className="font-medium">{screenName}</span> へのアクセス権限がありません。
          </p>

          {reason && (
            <div className="bg-gray-50 rounded-md p-4 mb-4">
              <p className="text-sm text-gray-700">{reason}</p>
            </div>
          )}

          <div className="mt-6">
            <p className="text-sm text-gray-500 mb-2">
              アクセス権限が必要な場合は、システム管理者に連絡してください。
            </p>
            <a
              href="/dashboard"
              className="inline-block text-sm text-blue-600 hover:text-blue-800 underline"
            >
              ダッシュボードに戻る
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
