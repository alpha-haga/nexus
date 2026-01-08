import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { authOptions } from '@/app/api/auth/[...nextauth]/route';
import { Header } from '@/modules/core/components/Header';

export default async function DashboardPage() {
  const session = await getServerSession(authOptions);

  if (!session) {
    redirect('/login');
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">ダッシュボード</h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* ユーザー情報カード */}
          <div className="card">
            <h3 className="text-lg font-medium text-gray-900 mb-4">
              ログイン情報
            </h3>
            <dl className="space-y-3">
              <div>
                <dt className="text-sm text-gray-500">氏名</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {session.user.name}
                </dd>
              </div>
              <div>
                <dt className="text-sm text-gray-500">メールアドレス</dt>
                <dd className="text-sm font-medium text-gray-900">
                  {session.user.email}
                </dd>
              </div>
              {session.user.corporationName && (
                <div>
                  <dt className="text-sm text-gray-500">所属法人</dt>
                  <dd className="text-sm font-medium text-gray-900">
                    {session.user.corporationName}
                  </dd>
                </div>
              )}
            </dl>
          </div>

          {/* 互助会プレースホルダ */}
          <div className="card">
            <h3 className="text-lg font-medium text-gray-900 mb-4">互助会</h3>
            <p className="text-sm text-gray-500">準備中</p>
          </div>

          {/* 葬祭プレースホルダ */}
          <div className="card">
            <h3 className="text-lg font-medium text-gray-900 mb-4">葬祭</h3>
            <p className="text-sm text-gray-500">準備中</p>
          </div>

          {/* 冠婚プレースホルダ */}
          <div className="card">
            <h3 className="text-lg font-medium text-gray-900 mb-4">冠婚</h3>
            <p className="text-sm text-gray-500">準備中</p>
          </div>

          {/* ポイントプレースホルダ */}
          <div className="card">
            <h3 className="text-lg font-medium text-gray-900 mb-4">ポイント</h3>
            <p className="text-sm text-gray-500">準備中</p>
          </div>

          {/* 管理プレースホルダ */}
          <div className="card">
            <h3 className="text-lg font-medium text-gray-900 mb-4">管理</h3>
            <p className="text-sm text-gray-500">準備中</p>
          </div>
        </div>
      </main>
    </div>
  );
}
