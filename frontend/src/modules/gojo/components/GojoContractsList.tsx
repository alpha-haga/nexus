'use client';

import { useState, useEffect } from 'react';
import { gojoService } from '@/services/gojo';
import type { GojoContract, PaginatedResponse, Region } from '@/types';

interface GojoContractsListProps {
  regionId: string;
  region: NonNullable<Region>;
}

export function GojoContractsList({ regionId, region }: GojoContractsListProps) {
  const [data, setData] = useState<PaginatedResponse<GojoContract> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState<20 | 50 | 100>(20);

  useEffect(() => {
    loadContracts();
  }, [regionId, region, page, size]);

  const loadContracts = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await gojoService.listLocal({ regionId, region, page, size });
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="card p-6 text-center">読み込み中...</div>;
  }

  if (error) {
    return <div className="card p-6 text-red-600">エラー: {error}</div>;
  }

  if (!data || data.content.length === 0) {
    return <div className="card p-6 text-center text-gray-500">契約が見つかりませんでした</div>;
  }

  return (
    <div className="space-y-4">
      <div className="card">
        <div className="p-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">契約一覧</h2>
            <select
              value={size}
              onChange={(e) => {
                setSize(Number(e.target.value) as 20 | 50 | 100);
                setPage(0);
              }}
              className="px-3 py-1 border border-gray-300 rounded"
            >
              <option value={20}>20件</option>
              <option value={50}>50件</option>
              <option value={100}>100件</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">契約ID</th>
                <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">プラン名</th>
                <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">契約日</th>
                <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">ステータス</th>
                <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">積立金額</th>
                <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">進捗率</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {data.content.map((contract) => (
                <tr key={contract.id} className="hover:bg-gray-50">
                  <td className="px-4 py-2 text-sm">{contract.id}</td>
                  <td className="px-4 py-2 text-sm">{contract.planName}</td>
                  <td className="px-4 py-2 text-sm">{contract.contractDate}</td>
                  <td className="px-4 py-2 text-sm">
                    <span className="px-2 py-1 text-xs rounded bg-blue-100 text-blue-800">
                      {contract.status}
                    </span>
                  </td>
                  <td className="px-4 py-2 text-sm">
                    ¥{contract.totalPaidAmount.toLocaleString()}
                  </td>
                  <td className="px-4 py-2 text-sm">
                    {(contract.progressRate * 100).toFixed(1)}%
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="p-4 border-t border-gray-200 flex items-center justify-between">
          <div className="text-sm text-gray-600">
            {data.totalElements}件中 {page * size + 1}-
            {Math.min((page + 1) * size, data.totalElements)}件を表示
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-3 py-1 border border-gray-300 rounded disabled:opacity-50"
            >
              前へ
            </button>
            <span className="px-3 py-1 text-sm">
              {page + 1} / {data.totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(data.totalPages - 1, page + 1))}
              disabled={page >= data.totalPages - 1}
              className="px-3 py-1 border border-gray-300 rounded disabled:opacity-50"
            >
              次へ
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}