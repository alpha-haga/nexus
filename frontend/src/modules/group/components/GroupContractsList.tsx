// frontend/src/modules/group/components/GroupContractsList.tsx（新規作成）

'use client';

import { useState, useEffect } from 'react';
import { groupService } from '@/services/group';
import type {
  GroupContractSearchCondition,
  PaginatedGroupContractResponse,
  Region,
  ApiError,
} from '@/types';
import { GroupContractSearchForm } from './GroupContractSearchForm';
import { RegionSelector } from './RegionSelector';

type SearchState = 'not-started' | 'loading' | 'success' | 'error';

export function GroupContractsList() {
  const [region, setRegion] = useState<Region | null>(null);
  const [result, setResult] = useState<PaginatedGroupContractResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState<20 | 50 | 100>(20);
  const [searchCondition, setSearchCondition] = useState<GroupContractSearchCondition>({});
  const [hasSearched, setHasSearched] = useState(false);
  const [searchState, setSearchState] = useState<SearchState>('not-started');

  // Region変更時に状態をリセット
  useEffect(() => {
    setResult(null);
    setError(null);
    setSearchState('not-started');
    setPage(0);
    setHasSearched(false);
  }, [region]);

  const loadContracts = async (params: {
    region: Region | null;
    condition: GroupContractSearchCondition;
    page: number;
    size: number;
  }) => {
    // Region未設定時はAPIを呼ばない
    if (!params.region) {
      return;
    }

    setIsLoading(true);
    setError(null);
    setSearchState('loading');

    try {
      const result = await groupService.searchContracts({
        ...params.condition,
        region: params.region,
        page: params.page,
        size: params.size,
      });
      setResult(result);
      setSearchState('success');
    } catch (err) {
      // ApiError として扱う
      const apiError: ApiError =
        err && typeof err === 'object' && 'status' in err
          ? (err as ApiError)
          : {
              timestamp: new Date().toISOString(),
              status: 500,
              error: 'Internal Server Error',
              message: err instanceof Error ? err.message : 'エラーが発生しました',
              code: 'UNKNOWN_ERROR',
            };
      setError(apiError);
      setResult(null);
      setSearchState('error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (condition: GroupContractSearchCondition) => {
    // Region未設定時は検索を実行しない
    if (!region) {
      return;
    }

    setSearchCondition(condition);
    setPage(0); // 検索時はページをリセット
    setHasSearched(true);
    loadContracts({
      region,
      condition,
      page: 0,
      size,
    });
  };

  // ページネーション変更時も検索を実行（検索実行済みの場合のみ）
  const handlePageChange = (newPage: number) => {
    if (hasSearched && region) {
      setPage(newPage);
      loadContracts({
        region,
        condition: searchCondition,
        page: newPage,
        size,
      });
    }
  };

  // ページサイズ変更時も検索を実行（検索実行済みの場合のみ）
  const handleSizeChange = (newSize: 20 | 50 | 100) => {
    if (hasSearched && region) {
      setSize(newSize);
      setPage(0);
      loadContracts({
        region,
        condition: searchCondition,
        page: 0,
        size: newSize,
      });
    }
  };

  // エラーメッセージを取得
  const getErrorMessage = (apiError: ApiError): string => {
    switch (apiError.status) {
      case 400:
        return `バリデーションエラー: ${apiError.message}`;
      case 403:
        return `権限エラー: ${apiError.message}`;
      case 404:
        return `リソースが見つかりません: ${apiError.message}`;
      case 500:
        return `サーバーエラー: ${apiError.message}`;
      default:
        return `エラー (${apiError.status}): ${apiError.message}`;
    }
  };

  // エラー表示のスタイルを取得
  const getErrorStyle = (status: number) => {
    switch (status) {
      case 400:
        return 'bg-amber-50 border-amber-200 text-amber-700';
      case 403:
        return 'bg-red-50 border-red-200 text-red-700';
      case 404:
        return 'bg-blue-50 border-blue-200 text-blue-700';
      case 500:
        return 'bg-red-50 border-red-200 text-red-700';
      default:
        return 'bg-red-50 border-red-200 text-red-700';
    }
  };

  return (
    <div className="space-y-4">
      {/* Region選択 */}
      <div className="card p-4">
        <RegionSelector value={region} onChange={setRegion} disabled={isLoading} />
        {!region && (
          <p className="mt-2 text-sm text-amber-600">
            Region を選択してください。Region が選択されていない場合、検索は実行できません。
          </p>
        )}
      </div>

      {/* 検索フォーム */}
      <GroupContractSearchForm
        onSearch={handleSearch}
        loading={isLoading}
        disabled={!region}
      />

      {/* エラー表示 */}
      {error && (
        <div className={`card p-4 border ${getErrorStyle(error.status)}`}>
          <p className="font-medium">エラー</p>
          <p className="text-sm mt-1">{getErrorMessage(error)}</p>
          {error.code && (
            <p className="text-xs mt-1 opacity-75">エラーコード: {error.code}</p>
          )}
          {error.correlationId && (
            <p className="text-xs mt-1 opacity-75">相関ID: {error.correlationId}</p>
          )}
        </div>
      )}

      {/* 未検索時 */}
      {!hasSearched && !isLoading && searchState === 'not-started' && (
        <div className="card p-6 text-center text-gray-500">
          {region
            ? '検索条件を入力して「検索」ボタンをクリックしてください'
            : 'Region を選択してから検索条件を入力してください'}
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
                  disabled={isLoading}
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
