// frontend/src/modules/group/components/GroupContractsList.tsx（新規作成）

'use client';

import { useState } from 'react';
import { groupService } from '@/services/group';
import type {
  GroupContractSearchCondition,
  PaginatedGroupContractResponse,
} from '@/types';
import { GroupContractSearchForm } from './GroupContractSearchForm';

export function GroupContractsList() {
  const [result, setResult] = useState<PaginatedGroupContractResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState<20 | 50 | 100>(20);
  const [searchCondition, setSearchCondition] = useState<GroupContractSearchCondition>({});
  const [hasSearched, setHasSearched] = useState(false);

  const loadContracts = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await groupService.searchContracts({
        ...searchCondition,
        page,
        size,
      });
      setResult(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
      setResult(null);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (condition: GroupContractSearchCondition) => {
    setSearchCondition(condition);
    setPage(0); // 検索時はページをリセット
    setHasSearched(true);
    loadContracts();
  };

  // ページネーション変更時も検索を実行（検索実行済みの場合のみ）
  const handlePageChange = (newPage: number) => {
    if (hasSearched) {
      setPage(newPage);
      loadContracts();
    }
  };

  // ページサイズ変更時も検索を実行（検索実行済みの場合のみ）
  const handleSizeChange = (newSize: 20 | 50 | 100) => {
    if (hasSearched) {
      setSize(newSize);
      setPage(0);
      loadContracts();
    }
  };

  return (
    <div className="space-y-4">
      <GroupContractSearchForm onSearch={handleSearch} loading={isLoading} />

      {/* エラー表示 */}
      {error && (
        <div className="card p-4 bg-red-50 border border-red-200 text-red-700">
          <p className="font-medium">エラー</p>
          <p className="text-sm mt-1">{error}</p>
        </div>
      )}

      {/* 未検索時 */}
      {!hasSearched && !isLoading && (
        <div className="card p-6 text-center text-gray-500">
          検索条件を入力して「検索」ボタンをクリックしてください
        </div>
      )}

      {/* 検索中 */}
      {isLoading && (
        <div className="card p-6 text-center text-gray-500">検索中...</div>
      )}

      {/* 検索結果: 0件 */}
      {!isLoading && hasSearched && result && result.content.length === 0 && (
        <div className="card p-6 text-center text-gray-500">
          検索条件に該当する契約が見つかりませんでした
        </div>
      )}

      {/* 検索結果: 1件以上 */}
      {!isLoading && result && result.content.length > 0 && (
        <div className="card">
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold">検索結果</h2>
              <div className="flex items-center gap-4">
                <span className="text-sm text-gray-600">
                  全{result.totalElements}件
                </span>
                <select
                  value={size}
                  onChange={(e) => handleSizeChange(Number(e.target.value) as 20 | 50 | 100)}
                  className="px-3 py-1 border border-gray-300 rounded"
                >
                  <option value={20}>20件</option>
                  <option value={50}>50件</option>
                  <option value={100}>100件</option>
                </select>
              </div>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    法人コード
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    法人名
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    契約番号
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    氏名（漢字）
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    氏名（カナ）
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    契約受付日
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    契約状態
                  </th>
                  <th className="px-4 py-2 text-left text-sm font-medium text-gray-700">
                    コース名
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {result.content.map((contract, index) => (
                  <tr key={`${contract.contractNo}-${index}`} className="hover:bg-gray-50">
                    <td className="px-4 py-2 text-sm">{contract.companyCd}</td>
                    <td className="px-4 py-2 text-sm">
                      {contract.companyShortName || '-'}
                    </td>
                    <td className="px-4 py-2 text-sm">{contract.contractNo}</td>
                    <td className="px-4 py-2 text-sm">
                      {contract.familyNameGaiji || ''}
                      {contract.firstNameGaiji || ''}
                      {!contract.familyNameGaiji && !contract.firstNameGaiji && '-'}
                    </td>
                    <td className="px-4 py-2 text-sm">
                      {contract.familyNameKana || ''}
                      {contract.firstNameKana || ''}
                      {!contract.familyNameKana && !contract.firstNameKana && '-'}
                    </td>
                    <td className="px-4 py-2 text-sm">
                      {contract.contractReceiptYmd || '-'}
                    </td>
                    <td className="px-4 py-2 text-sm">
                      {contract.contractStatus || contract.contractStatusKbn || '-'}
                    </td>
                    <td className="px-4 py-2 text-sm">
                      {contract.courseName || contract.courseCd || '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="p-4 border-t border-gray-200 flex items-center justify-between">
            <div className="text-sm text-gray-600">
              {result.totalElements}件中 {page * size + 1}-
              {Math.min((page + 1) * size, result.totalElements)}件を表示
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => handlePageChange(Math.max(0, page - 1))}
                disabled={page === 0 || isLoading}
                className="px-3 py-1 border border-gray-300 rounded disabled:opacity-50"
              >
                前へ
              </button>
              <span className="px-3 py-1 text-sm">
                {page + 1} / {result.totalPages}
              </span>
              <button
                onClick={() => handlePageChange(Math.min(result.totalPages - 1, page + 1))}
                disabled={page >= result.totalPages - 1 || isLoading}
                className="px-3 py-1 border border-gray-300 rounded disabled:opacity-50"
              >
                次へ
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
