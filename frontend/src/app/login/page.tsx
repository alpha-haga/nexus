'use client';

import { signIn, useSession } from 'next-auth/react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';

export default function LoginPage() {
  const { status } = useSession();
  const router = useRouter();
  const params = useSearchParams();
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const queryError = useMemo(() => params.get('error'), [params]);

  useEffect(() => {
    if (status === 'authenticated') router.replace('/dashboard');
  }, [status, router]);

  useEffect(() => {
    if (queryError) setError('ログインに失敗しました（再度お試しください）');
  }, [queryError]);

  const handleKeycloakLogin = async () => {
    setError('');
    setIsLoading(true);

    const res = await signIn('keycloak', {
      callbackUrl: '/dashboard',
      redirect: false,
    });

    if (res?.error) {
      setError('ログインに失敗しました（Keycloak設定や権限を確認してください）');
      setIsLoading(false);
      return;
    }

    router.replace(res?.url ?? '/dashboard');
  };

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-gray-500">読み込み中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="max-w-sm w-full">
        <div className="card">
          <div className="text-center mb-8">
            <h1 className="text-2xl font-bold text-primary-700 mb-2">NEXUS</h1>
            <p className="text-sm text-gray-500">グループ法人 統合基幹システム</p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-danger-50 border border-danger-200 rounded-md">
              <p className="text-sm text-danger-700">{error}</p>
            </div>
          )}

          <button
            onClick={handleKeycloakLogin}
            disabled={isLoading}
            className="btn-primary w-full"
          >
            {isLoading ? 'ログイン中...' : 'Keycloakでログイン'}
          </button>

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
