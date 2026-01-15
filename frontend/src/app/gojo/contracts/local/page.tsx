import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { AppLayout } from '@/modules/core';
import { GojoContractsList } from '@/modules/gojo';

export default async function GojoContractsLocalPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  // TODO: regionId をセッションまたはクエリパラメータから取得
  const regionId = 'tokyo'; // 仮実装

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <div className="mb-6">
        <h1 className="text-xl font-semibold text-gray-900">互助会契約 - 地区内</h1>
          <p className="text-sm text-gray-500 mt-1">地区内の契約一覧</p>
        </div>
        <GojoContractsList regionId={regionId} />
      </div>
    </AppLayout>
  );
}