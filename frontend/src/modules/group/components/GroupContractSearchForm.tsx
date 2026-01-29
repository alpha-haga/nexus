// frontend/src/modules/group/components/GroupContractSearchForm.tsx（新規作成）

'use client';

import { useState, FormEvent } from 'react';
import type { GroupContractSearchCondition } from '@/types';

interface GroupContractSearchFormProps {
  onSearch: (condition: GroupContractSearchCondition) => void;
  loading?: boolean;
}

export function GroupContractSearchForm({
  onSearch,
  loading = false,
}: GroupContractSearchFormProps) {
  const [condition, setCondition] = useState<GroupContractSearchCondition>({});

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSearch(condition);
  };

  const handleReset = () => {
    setCondition({});
    onSearch({});
  };

  return (
    <form onSubmit={handleSubmit} className="card p-4 space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {/* 契約受付日（開始） */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            契約受付日（開始）
          </label>
          <input
            type="text"
            placeholder="YYYYMMDD"
            value={condition.contractReceiptYmdFrom || ''}
            onChange={(e) =>
              setCondition({ ...condition, contractReceiptYmdFrom: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
            maxLength={8}
          />
        </div>

        {/* 契約受付日（終了） */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            契約受付日（終了）
          </label>
          <input
            type="text"
            placeholder="YYYYMMDD"
            value={condition.contractReceiptYmdTo || ''}
            onChange={(e) =>
              setCondition({ ...condition, contractReceiptYmdTo: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
            maxLength={8}
          />
        </div>

        {/* 契約番号 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            契約番号
          </label>
          <input
            type="text"
            value={condition.contractNo || ''}
            onChange={(e) =>
              setCondition({ ...condition, contractNo: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>

        {/* 姓カナ */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            姓（カナ）
          </label>
          <input
            type="text"
            value={condition.familyNmKana || ''}
            onChange={(e) =>
              setCondition({ ...condition, familyNmKana: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>

        {/* 電話番号 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            電話番号
          </label>
          <input
            type="text"
            value={condition.telNo || ''}
            onChange={(e) =>
              setCondition({ ...condition, telNo: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>

        {/* 募集コード */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            募集コード
          </label>
          <input
            type="text"
            value={condition.bosyuCd || ''}
            onChange={(e) =>
              setCondition({ ...condition, bosyuCd: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>

        {/* コースコード */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            コースコード
          </label>
          <input
            type="text"
            value={condition.courseCd || ''}
            onChange={(e) =>
              setCondition({ ...condition, courseCd: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>

        {/* 契約状態区分 */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            契約状態区分
          </label>
          <input
            type="text"
            value={condition.contractStatusKbn || ''}
            onChange={(e) =>
              setCondition({ ...condition, contractStatusKbn: e.target.value || undefined })
            }
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
        </div>
      </div>

      <div className="flex gap-2 pt-2">
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? '検索中...' : '検索'}
        </button>
        <button
          type="button"
          onClick={handleReset}
          disabled={loading}
          className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50"
        >
          リセット
        </button>
      </div>
    </form>
  );
}