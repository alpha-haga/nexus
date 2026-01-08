'use client';

import { signIn, useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

export default function LoginPage() {
  const { status } = useSession();
  const router = useRouter();

  useEffect(() => {
    if (status === 'authenticated') {
      router.replace('/dashboard');
    }
  }, [status, router]);

  const handleLogin = () => {
    signIn('keycloak', { callbackUrl: '/dashboard' });
  };

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-gray-500">読み込み中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full">
        <div className="card">
          <div className="text-center mb-8">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">NEXUS</h1>
            <p className="text-sm text-gray-600">
              互助会・葬祭・冠婚・ポイント統合システム
            </p>
          </div>
          <div className="space-y-4">
            <p className="text-center text-sm text-gray-600">
              社員アカウントでログインしてください
            </p>
            <button
              onClick={handleLogin}
              className="btn-primary w-full flex items-center justify-center gap-2"
            >
              ログイン
            </button>
          </div>
          <div className="mt-6 pt-6 border-t border-gray-200">
            <p className="text-xs text-center text-gray-500">
              ログインに問題がある場合は、システム管理者にお問い合わせください
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
