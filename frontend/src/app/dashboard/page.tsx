import { getServerSession } from 'next-auth';
import { redirect } from 'next/navigation';
import { getAuthOptions } from '@/app/api/auth/[...nextauth]/auth-options';
import { AppLayout } from '@/modules/core';
import Link from 'next/link';

// モックデータ: 実際のAPIに接続しやすい構造
export const dynamic = 'force-dynamic';

// State/Phase は docs/architecture/dependency-rules.md 準拠
const mockDashboardData = {
  slaMetrics: {
    exceededCount: 3,
    warningCount: 8,
    totalActive: 156,
  },
  phaseMetrics: {
    preparation: 24,
    inProgress: 67,
    document: 12,
    completed: 53,
  },
  resubmissionRate: {
    rate: 4.2,
    total: 48,
    resubmitted: 2,
  },
  monthlyProgress: {
    month: '2026年1月',
    processed: 142,
    target: 200,
    percentage: 71,
  },
  domainCounts: {
    gojo: { active: 1250, thisMonth: 45 },
    funeral: { active: 23, thisMonth: 8 },
    agent: { active: 89, thisMonth: 12 },
  },
};

interface StatCardProps {
  label: string;
  value: number | string;
  subLabel?: string;
  status?: 'normal' | 'warning' | 'danger';
  href?: string;
}

function StatCard({ label, value, subLabel, status = 'normal', href }: StatCardProps) {
  const valueClass =
    status === 'danger'
      ? 'stat-value stat-danger'
      : status === 'warning'
      ? 'stat-value stat-warning'
      : 'stat-value';

  const content = (
    <div className="stat-card hover:border-gray-300 transition-colors">
      <p className={valueClass}>{value}</p>
      <p className="stat-label">{label}</p>
      {subLabel && <p className="text-xs text-gray-400 mt-1">{subLabel}</p>}
    </div>
  );

  if (href) {
    return (
      <Link href={href} className="block">
        {content}
      </Link>
    );
  }

  return content;
}

export default async function DashboardPage() {
  const session = await getServerSession(getAuthOptions());

  if (!session) {
    redirect('/login');
  }

  const data = mockDashboardData;

  return (
    <AppLayout>
      <div className="max-w-6xl">
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-gray-900">ダッシュボード</h1>
          <p className="text-sm text-gray-500 mt-1">業務状況の概要（読み取り専用）</p>
        </div>

        {/* SLA・アラート指標 */}
        <section className="mb-8">
          <h2 className="text-sm font-medium text-gray-700 mb-3">SLA・アラート</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard
              label="SLA超過"
              value={data.slaMetrics.exceededCount}
              subLabel="件"
              status={data.slaMetrics.exceededCount > 0 ? 'danger' : 'normal'}
              href="/reporting"
            />
            <StatCard
              label="SLA注意"
              value={data.slaMetrics.warningCount}
              subLabel="件"
              status={data.slaMetrics.warningCount > 5 ? 'warning' : 'normal'}
              href="/reporting"
            />
            <StatCard
              label="再提出率"
              value={`${data.resubmissionRate.rate}%`}
              subLabel={`${data.resubmissionRate.resubmitted}/${data.resubmissionRate.total}件`}
              status={data.resubmissionRate.rate > 5 ? 'warning' : 'normal'}
            />
            <StatCard
              label="処理中タスク"
              value={data.slaMetrics.totalActive}
              subLabel="件"
            />
          </div>
        </section>

        {/* フェーズ別滞留状況 */}
        <section className="mb-8">
          <h2 className="text-sm font-medium text-gray-700 mb-3">フェーズ別滞留状況</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard
              label="準備中"
              value={data.phaseMetrics.preparation}
              subLabel="PREPARATION"
            />
            <StatCard
              label="処理中"
              value={data.phaseMetrics.inProgress}
              subLabel="IN_PROGRESS"
            />
            <StatCard
              label="書類作成"
              value={data.phaseMetrics.document}
              subLabel="DOCUMENT"
              status={data.phaseMetrics.document > 10 ? 'warning' : 'normal'}
            />
            <StatCard
              label="完了"
              value={data.phaseMetrics.completed}
              subLabel="COMPLETED"
            />
          </div>
        </section>

        {/* 月次処理進捗 */}
        <section className="mb-8">
          <h2 className="text-sm font-medium text-gray-700 mb-3">月次処理進捗</h2>
          <div className="card">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-gray-600">{data.monthlyProgress.month}</span>
              <span className="text-sm font-medium text-gray-900">
                {data.monthlyProgress.processed} / {data.monthlyProgress.target} 件
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-primary-600 h-2 rounded-full transition-all"
                style={{ width: `${data.monthlyProgress.percentage}%` }}
              />
            </div>
            <p className="text-xs text-gray-500 mt-2">
              進捗率: {data.monthlyProgress.percentage}%
            </p>
          </div>
        </section>

        {/* 業務別件数 */}
        <section>
          <h2 className="text-sm font-medium text-gray-700 mb-3">業務別件数</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Link href="/gojo" className="block">
              <div className="card hover:border-gray-300 transition-colors">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-medium text-gray-900">互助会</h3>
                    <p className="text-xs text-gray-500">nexus-gojo</p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-semibold text-gray-900">
                      {data.domainCounts.gojo.active.toLocaleString()}
                    </p>
                    <p className="text-xs text-gray-500">
                      今月 +{data.domainCounts.gojo.thisMonth}
                    </p>
                  </div>
                </div>
              </div>
            </Link>

            <Link href="/funeral" className="block">
              <div className="card hover:border-gray-300 transition-colors">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-medium text-gray-900">葬祭</h3>
                    <p className="text-xs text-gray-500">nexus-funeral</p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-semibold text-gray-900">
                      {data.domainCounts.funeral.active}
                    </p>
                    <p className="text-xs text-gray-500">
                      今月 +{data.domainCounts.funeral.thisMonth}
                    </p>
                  </div>
                </div>
              </div>
            </Link>

            <Link href="/agent" className="block">
              <div className="card hover:border-gray-300 transition-colors">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-medium text-gray-900">代理店</h3>
                    <p className="text-xs text-gray-500">nexus-agent</p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-semibold text-gray-900">
                      {data.domainCounts.agent.active}
                    </p>
                    <p className="text-xs text-gray-500">
                      今月 +{data.domainCounts.agent.thisMonth}
                    </p>
                  </div>
                </div>
              </div>
            </Link>
          </div>
        </section>
      </div>
    </AppLayout>
  );
}
