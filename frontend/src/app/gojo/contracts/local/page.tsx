import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { getAuthOptions } from '@/app/api/auth/[...nextauth]/auth-options';
import { AppLayout } from '@/modules/core';

export const dynamic = 'force-dynamic';

export default async function GojoContractsPage() {
  const session = await getServerSession(getAuthOptions());

  if (!session) {
    redirect('/login');
  }

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-gray-900">互助会契約一覧</h1>
          <p className="text-sm text-gray-500 mt-1">契約一覧（Local / All は画面内切替）</p>
        </div>
        {/* TODO: Local / All 切替UIを実装 */}
      </div>
    </AppLayout>
  );
}