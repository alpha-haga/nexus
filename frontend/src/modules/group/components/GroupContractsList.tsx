'use client';

import Link from 'next/link';
import { useState, useEffect, useRef } from 'react';
import { groupService } from '@/services/group';
import type {
  GroupContractSearchResponse,
  GroupContractSearchCondition,
  PaginatedGroupContractResponse,
  Region,
  ApiError,
} from '@/types';
import { GroupContractSearchForm } from './GroupContractSearchForm';
import { RegionSelector } from './RegionSelector';
import { TenantSelector } from '@/modules/core/components/TenantSelector';
import {
  saveListState,
  restoreListState,
  buildQueryKey,
  saveTenant,
  clearListState,
  getSavedState,
} from '../utils/sessionStorage';
import { useTenantContext } from '@/contexts/TenantContext';

type SearchState = 'not-started' | 'loading' | 'success' | 'error';
type ColumnPreset = 'standard' | 'contact' | 'staff';
type ColumnKey = 'cmpShortName' | 'contractNo' | 'familyNameGaiji' | 'contractReceiptYmd' | 'birthday' | 'contractStatus' | 'courseName' | 'bosyuStaff' | 'entryStaff' | 'address' | 'telNo' | 'mobileNo' | 'shareNum' | 'monthlyPremium' | 'contractGaku' | 'totalSaveNum' | 'totalGaku' | 'motoSupplyRankOrgCd' | 'motoSupplyRankOrgName';

// 桁あふれ処理用のユーティリティ（Wave 0-B）
const truncateText = (text: string | null | undefined, maxLength: number): string => {
  // null または undefined の場合のみ '-' を返す
  if (text === null || text === undefined) return '-';
  // 空文字 '' はそのまま返す
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// 日付フォーマット（YYYYMMDD → YYYY/MM/DD）
const formatDate = (ymd: string | null | undefined): string => {
  if (!ymd || ymd.length !== 8) return '-';
  return `${ymd.substring(0, 4)}/${ymd.substring(4, 6)}/${ymd.substring(6, 8)}`;
};

// 金額フォーマット（カンマ区切り、単位: 円）
const formatAmount = (amount: number | null | undefined): string => {
  if (amount === null || amount === undefined) return '-';
  return `${Intl.NumberFormat('ja-JP').format(amount)}円`;
};

// 数値フォーマット（回数・口数、null は '-'）
const formatNumber = (num: number | null | undefined): string => {
  if (num === null || num === undefined) return '-';
  return num.toString();
};

// 住所連結用ユーティリティ
// null/undefined/空文字/空白のみを除外して正規化
const normalize = (s: string | null | undefined): string | null => {
  if (s === null || s === undefined) return null;
  const trimmed = s.trim();
  return trimmed === '' ? null : trimmed;
};

// 住所項目を連結（日本住所は区切り無しが自然）
type AddressParts = Pick<GroupContractSearchResponse, 'prefName' | 'cityTownName' | 'oazaTownName' | 'azaChomeName' | 'addr1' | 'addr2'>;
const buildAddress = (contract: AddressParts): string => {
  const parts = [normalize(contract.prefName), normalize(contract.cityTownName), normalize(contract.oazaTownName), normalize(contract.azaChomeName), normalize(contract.addr1), normalize(contract.addr2)];
  const joined = parts.filter((p): p is string => p !== null).join('');
  return joined === '' ? '-' : joined;
};

// 列表示プリセット定義
const COLUMN_PRESETS: Record<ColumnPreset, Set<ColumnKey>> = {
  standard: new Set<ColumnKey>([
    'cmpShortName', 'contractNo', 'familyNameGaiji',
    'contractReceiptYmd', 'birthday', 'contractStatus', 'courseName',
    'bosyuStaff', 'entryStaff', 'address', 'telNo', 'mobileNo',
    'shareNum', 'monthlyPremium', 'contractGaku', 'totalSaveNum', 'totalGaku',
    'motoSupplyRankOrgCd', 'motoSupplyRankOrgName'
  ]),
  contact: new Set<ColumnKey>([
    'cmpShortName', 'contractNo', 'familyNameGaiji',
    'contractReceiptYmd', 'contractStatus', 'address', 'telNo', 'mobileNo'
  ]),
  staff: new Set<ColumnKey>([
    'cmpShortName', 'contractNo', 'familyNameGaiji',
    'contractReceiptYmd', 'contractStatus', 'bosyuStaff', 'entryStaff'
  ])
};

// 適用中フィルタサマリを生成（選択されている条件のみ表示）
const buildFilterSummary = (condition: GroupContractSearchCondition): string[] => {
  const items: string[] = [];
  
  // 法人（選択件数）- 入力がある場合のみ
  if (condition.cmpCds && condition.cmpCds.length > 0) {
    items.push(`法人:${condition.cmpCds.length}件`);
  }
  
  // 契約番号 - 入力がある場合のみ
  if (condition.contractNo) {
    items.push(`契約番号:${condition.contractNo}`);
  }
  
  // 契約者氏名 - 入力がある場合のみ
  if (condition.contractorName) {
    items.push(`契約者氏名:${condition.contractorName}`);
  }
  
  // 電話番号 - 入力がある場合のみ
  if (condition.telNo) {
    items.push(`電話番号:${condition.telNo}`);
  }
  
  // 契約状態区分 - 入力がある場合のみ
  if (condition.contractStatusKbn) {
    items.push(`契約状態区分:${condition.contractStatusKbn}`);
  }
  
  // 契約受付日 - 入力がある場合のみ
  if (condition.contractReceiptYmdFrom || condition.contractReceiptYmdTo) {
    const dateParts: string[] = [];
    if (condition.contractReceiptYmdFrom) {
      dateParts.push(condition.contractReceiptYmdFrom);
    }
    dateParts.push('〜');
    if (condition.contractReceiptYmdTo) {
      dateParts.push(condition.contractReceiptYmdTo);
    }
    items.push(`受付日:${dateParts.join('')}`);
  }
  
  // 募集コード - 入力がある場合のみ
  if (condition.bosyuCd) {
    items.push(`募集コード:${condition.bosyuCd}`);
  }
  
  // 担当者氏名 - 入力がある場合のみ
  if (condition.staffName) {
    items.push(`担当者氏名:${condition.staffName}`);
  }
  
  // コースコード - 入力がある場合のみ
  if (condition.courseCd) {
    items.push(`コースコード:${condition.courseCd}`);
  }
  
  // コース名 - 入力がある場合のみ
  if (condition.courseName) {
    items.push(`コース名:${condition.courseName}`);
  }
  
  return items;
};

// 固定列の幅とleft位置を定数で定義
const STICKY_COLUMN_WIDTHS = {
  cmpShortName: 120,
  contractNo: 120,
  familyNameGaiji: 180,
  tel: 140,
} as const;

const STICKY_COLUMN_LEFTS = {
  cmpShortName: 0,
  contractNo: STICKY_COLUMN_WIDTHS.cmpShortName,
  familyNameGaiji: STICKY_COLUMN_WIDTHS.cmpShortName + STICKY_COLUMN_WIDTHS.contractNo,
  tel: STICKY_COLUMN_WIDTHS.cmpShortName + STICKY_COLUMN_WIDTHS.contractNo + STICKY_COLUMN_WIDTHS.familyNameGaiji,
} as const;

export function GroupContractsList() {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [region, setRegion] = useState<Region | null>(null);
  const [result, setResult] = useState<PaginatedGroupContractResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState<20 | 50 | 100>(20);
  const [searchCondition, setSearchCondition] = useState<GroupContractSearchCondition>({});
  const [hasSearched, setHasSearched] = useState(false);
  const [searchState, setSearchState] = useState<SearchState>('not-started');
  const [isSearchFormOpen, setIsSearchFormOpen] = useState(false);
  const [columnPreset, setColumnPreset] = useState<ColumnPreset>('standard');
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const hasRestoredRef = useRef(false);
  const restoringRegionRef = useRef(false);
  const restoringTenantRef = useRef(false);
  const prevTenantRef = useRef<string | null>(null);
  const { selectedTenant, setTenant } = useTenantContext();

  // 初回マウント時に region と tenant を自動復元
  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (region !== null && selectedTenant !== null) return;
  
    const saved = getSavedState();
    if (!saved) return;
  
    if (saved.region && region === null) {
      restoringRegionRef.current = true;
      setRegion(saved.region);
    }
    if (saved.tenant && selectedTenant === null) {
      restoringTenantRef.current = true;
      setTenant(saved.tenant);
    }
  }, [region, selectedTenant, setTenant]);

  // Region変更時に状態をリセット
  useEffect(() => {
    if (region === null) return;
  
    if (restoringRegionRef.current) {
      restoringRegionRef.current = false;
      return;
    }
  
    clearListState();
  
    setResult(null);
    setError(null);
    setSearchState('not-started');
    setPage(0);
    setHasSearched(false);
    setIsSearchFormOpen(false);
    setSearchCondition({});
    setSelectedKey(null);
    hasRestoredRef.current = false;
  }, [region]);

  // Tenant変更時に状態をリセット（ユーザー操作由来の場合のみ）
  useEffect(() => {
    // selectedTenant の実変更を検知（同じ値なら何もしない）
    if (prevTenantRef.current === selectedTenant) {
      return;
    }

    // selectedTenant が null になっただけでは一覧をクリアしない（揺れによる誤発火を防止）
    if (selectedTenant === null) {
      prevTenantRef.current = selectedTenant;
      return;
    }

    // 初回マウント直後は何もしない（復元処理が完了するまで待つ）
    // hasRestoredRef で復元処理の完了を判定（hasSearched / searchState は依存配列に入れない）
    if (!hasRestoredRef.current) {
      prevTenantRef.current = selectedTenant;
      return;
    }
  
    if (restoringTenantRef.current) {
      prevTenantRef.current = selectedTenant;
      restoringTenantRef.current = false;
      saveTenant(selectedTenant);
      return;
    }
  
    // 一覧状態をクリア
    clearListState();
  
    setResult(null);
    setError(null);
    setSearchState('not-started');
    setPage(0);
    setHasSearched(false);
    setIsSearchFormOpen(false);
    setSearchCondition({});
    setSelectedKey(null);
    hasRestoredRef.current = false;
    
    // tenant の永続化（selectedTenant は非nullなので saveTenant のみ）
    saveTenant(selectedTenant);

    // クリア処理後に prevTenantRef を更新（次の比較のため）
    prevTenantRef.current = selectedTenant;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  // 依存配列は selectedTenant のみ（hasSearched / searchState を入れないことで、検索ボタン押下時に effect が再実行されないようにする）
  }, [selectedTenant]);

  // region 確定後に sessionStorage から状態を復元（1回のみ）
  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (!region) return; // region が確定するまで待つ
    if (hasRestoredRef.current) return; // 既に復元済みならスキップ
    
    const saved = getSavedState();
    if (!saved) {
      // saved が無い場合でも初期化フェーズは完了したとマーク（復元が無いケースでも Tenant変更effect が正常に動作するようにする）
      hasRestoredRef.current = true;
      return;
    }
    
    // tenant整合チェック: saved.tenant と selectedTenant が一致しない場合は復元しない
    if (saved.tenant && saved.tenant !== selectedTenant) {
      // tenant不一致でも初期化フェーズは完了したとマーク
      hasRestoredRef.current = true;
      return;
    }
    
    // saved.region と region が一致しないなら復元しない
    if (saved.region !== region) {
      // region不一致でも初期化フェーズは完了したとマーク
      hasRestoredRef.current = true;
      return;
    }

    // 復元確定ロック（以降の再実行を防ぐ）
    hasRestoredRef.current = true;

    // 保存された条件を設定（空の場合のみ）
    if (Object.keys(searchCondition).length === 0 && saved.condition) {
      setSearchCondition(saved.condition);
    }

    // 保存されたページネーション設定を適用
    if (saved.page !== undefined) setPage(saved.page);
    if (saved.size !== undefined) setSize(saved.size as 20 | 50 | 100);
    if (saved.columnPreset !== undefined) setColumnPreset(saved.columnPreset);

    // 保存された結果を表示
    if (saved.result) {
      setResult(saved.result);
      setSearchState('success');
      setHasSearched(true);
    }

    // 選択行とスクロール位置を復元
    if (saved.selectedKey) {
      setSelectedKey(saved.selectedKey);
    }
    if (saved.scrollY !== undefined && scrollContainerRef.current) {
      requestAnimationFrame(() => {
        if (scrollContainerRef.current) {
          scrollContainerRef.current.scrollTop = saved.scrollY!;
        }
      });
    }
  }, [region, searchCondition, selectedTenant]); // region 確定後に1回だけ復元

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
    
    // キャッシュチェック: 同一条件なら sessionStorage から復元
    const queryKey = buildQueryKey(selectedTenant, params.condition, params.page, params.size, columnPreset);
    const cached = restoreListState(selectedTenant, params.region, params.condition, params.page, params.size, columnPreset);
    if (cached && cached.result && cached.queryKey === queryKey) {
      setResult(cached.result);
      setSearchState('success');
      setIsLoading(false);
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
      // 検索結果を sessionStorage に保存
      saveListState(selectedTenant, params.region, params.condition, params.page, params.size, columnPreset, result);
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
    if (!region || !selectedTenant) {
      return;
    }
  
    setSearchCondition(condition);
    setPage(0); // 検索時はページをリセット
    setHasSearched(true);
    setSelectedKey(null);
    setIsSearchFormOpen(false); // 検索後に折りたたむ
    loadContracts({
      region,
      condition,
      page: 0,
      size,
    });
  };
  
  // 詳細への遷移時に状態を保存
  const handleDetailClick = (cmpCd: string, contractNo: string) => {
    if (!region || !result) return;
    
    const scrollY = scrollContainerRef.current?.scrollTop ?? 0;
    
    saveListState(selectedTenant, region, searchCondition, page, size, columnPreset, result, {
      selectedKey: `${cmpCd}:${contractNo}`,
      scrollY,
    });
  };

  const handleConditionChange = (next: GroupContractSearchCondition) => {
    setSearchCondition(next);
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

  const filterSummary = buildFilterSummary(searchCondition);
  const visibleColumns = COLUMN_PRESETS[columnPreset];
  
  // 折りたたみ状態に応じたテーブル高さを計算
  const tableMaxHeight = isSearchFormOpen
    ? 'calc(100vh - 520px)' // 展開時はフォーム分の高さを確保
    : 'calc(100vh - 260px)'; // 折りたたみ時は高さを抑える（ページスクロールを出さない）

  return (
    <div className="space-y-4 min-h-0">
      {/* Tenant選択 */}
      <div className="card p-4">
        <TenantSelector />
      </div>
      {/* Region選択 */}
      <div className="card p-4">
        <RegionSelector value={region} onChange={setRegion} disabled={isLoading || !selectedTenant} />
        {!region && (
          <p className="mt-2 text-sm text-amber-600">
            Region を選択してください。Region が選択されていない場合、検索は実行できません。
          </p>
        )}
        {!selectedTenant && (
          <p className="mt-2 text-sm text-amber-600">
            法人を選択してください。法人が選択されていない場合、Region は選択できません。
          </p>
        )}
      </div>

      {/* 検索条件 */}
      <div className="card">
        <div>
          <button
            type="button"
            onClick={() => setIsSearchFormOpen(!isSearchFormOpen)}
            className="w-full p-3 flex items-center justify-between hover:bg-gray-50 transition-colors"
          >
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-gray-700">検索条件（追加）</span>
              {filterSummary.length > 0 ? (
                <span className="text-xs text-gray-500">
                  （{filterSummary.join(' / ')}）
                </span>
              ) : null}
            </div>
            <svg
              className={`w-5 h-5 text-gray-500 transition-transform ${isSearchFormOpen ? 'rotate-180' : ''}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          {isSearchFormOpen && (
            <div className="border-t border-gray-200 p-4">
              <GroupContractSearchForm
                variant="full"
                value={searchCondition}
                onChange={handleConditionChange}
                onSearch={handleSearch}
                loading={isLoading}
                disabled={!region || !selectedTenant}
                showActions={true}
              />
            </div>
          )}
        </div>
      </div>

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
        <div className="card overflow-hidden flex flex-col min-h-0">
          <div className="p-4 border-b border-gray-200 flex-shrink-0">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold">検索結果</h2>
              <div className="flex items-center gap-4">
                <span className="text-sm text-gray-600">
                  全{result.totalElements}件
                </span>
                <select
                  value={columnPreset}
                  onChange={(e) => setColumnPreset(e.target.value as ColumnPreset)}
                  className="px-3 py-1 border border-gray-300 rounded text-sm"
                  disabled={isLoading}
                >
                  <option value="standard">標準</option>
                  <option value="contact">連絡先</option>
                  <option value="staff">担当者</option>
                </select>
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

          <div ref={scrollContainerRef} className="flex-1 min-h-0 overflow-auto" style={{ maxHeight: tableMaxHeight }}>
            <table className="w-full min-w-[1600px] border-separate border-spacing-0">
              <thead>
                <tr>
                  {/* 固定列（DOM先頭） */}
                  <th 
                    className="px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-40"
                    style={{ left: `${STICKY_COLUMN_LEFTS.cmpShortName}px`, width: `${STICKY_COLUMN_WIDTHS.cmpShortName}px` }}
                  >
                    法人名
                  </th>
                  <th 
                    className="px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-40"
                    style={{ left: `${STICKY_COLUMN_LEFTS.contractNo}px`, width: `${STICKY_COLUMN_WIDTHS.contractNo}px` }}
                  >
                    契約番号
                  </th>
                  <th 
                    className="px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-40"
                    style={{ left: `${STICKY_COLUMN_LEFTS.familyNameGaiji}px`, width: `${STICKY_COLUMN_WIDTHS.familyNameGaiji}px` }}
                  >
                    契約者氏名
                  </th>
                  <th 
                    className="px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-40"
                    style={{ left: `${STICKY_COLUMN_LEFTS.tel}px`, width: `${STICKY_COLUMN_WIDTHS.tel}px` }}
                  >
                    電話
                  </th>
                  {/* 横スクロール対象列 */}
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('contractReceiptYmd') ? '' : 'hidden'}`}>
                    契約受付日
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('birthday') ? '' : 'hidden'}`}>
                    生年月日
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('contractStatus') ? '' : 'hidden'}`}>
                    契約状態
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('courseName') ? '' : 'hidden'}`}>
                    コース名
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('bosyuStaff') ? '' : 'hidden'}`}>
                    募集担当者
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('entryStaff') ? '' : 'hidden'}`}>
                    加入担当者
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('address') ? '' : 'hidden'}`}>
                    住所
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('shareNum') ? '' : 'hidden'}`}>
                    口数
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('monthlyPremium') ? '' : 'hidden'}`}>
                    月掛金
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('contractGaku') ? '' : 'hidden'}`}>
                    契約金額
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('totalSaveNum') ? '' : 'hidden'}`}>
                    積立回数
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('totalGaku') ? '' : 'hidden'}`}>
                    積立金額
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('motoSupplyRankOrgCd') ? '' : 'hidden'}`}>
                    元請支給ランク組織コード
                  </th>
                  <th className={`px-4 py-2 text-left text-sm font-medium text-gray-700 sticky top-0 bg-gray-50 z-30 ${visibleColumns.has('motoSupplyRankOrgName') ? '' : 'hidden'}`}>
                    元請支給ランク組織名
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {result.content.map((contract, index) => {
                  // 募集担当者名の結合
                  const bosyuName = `${contract.bosyuFamilyNameKanji || ''}${contract.bosyuFirstNameKanji || ''}`;
                  const displayBosyuName = bosyuName === '' ? '-' : truncateText(bosyuName, 20);
                  const bosyuTitle = bosyuName === '' ? undefined : bosyuName;

                  // 加入担当者名の結合
                  const entryName = `${contract.entryFamilyNameKanji || ''}${contract.entryFirstNameKanji || ''}`;
                  const displayEntryName = entryName === '' ? '-' : truncateText(entryName, 20);
                  const entryTitle = entryName === '' ? undefined : entryName;

                  // 住所の連結
                  const addressText = buildAddress(contract);
                  const displayAddress = truncateText(addressText, 30);
                  const addressTitle = addressText;

                  // 契約者氏名（漢字＋カナ）
                  const familyNameKanji = contract.familyNameGaiji || '';
                  const firstNameKanji = contract.firstNameGaiji || '';
                  const familyNameKana = contract.familyNameKana || '';
                  const firstNameKana = contract.firstNameKana || '';
                  const nameKanji = `${familyNameKanji}${firstNameKanji}`;
                  const nameKana = `${familyNameKana}${firstNameKana}`;
                  const nameTitle = nameKanji && nameKana ? `${nameKanji} ${nameKana}` : nameKanji || nameKana || undefined;

                  // 電話（TEL/MOBILE 2段表示）
                  const telNo = contract.telNo || '-';
                  const mobileNo = contract.mobileNo || '-';

                  const isSelected = selectedKey === `${contract.cmpCd}:${contract.contractNo}`;
                  return (
                    <tr 
                      key={`${contract.contractNo}-${index}`} 
                      className={`group hover:bg-gray-50 ${isSelected ? 'bg-blue-50 border-l-4 border-l-blue-500' : ''}`}
                    >
                      {/* 固定列（DOM先頭） */}
                      <td 
                        className="px-4 py-2 text-sm sticky bg-white group-hover:bg-gray-50 z-20"
                        style={{ left: `${STICKY_COLUMN_LEFTS.cmpShortName}px`, width: `${STICKY_COLUMN_WIDTHS.cmpShortName}px` }}
                        title={contract.cmpShortName || undefined}
                      >
                        {truncateText(contract.cmpShortName, 20)}
                      </td>
                      <td 
                        className="px-4 py-2 text-sm sticky bg-white group-hover:bg-gray-50 z-20"
                        style={{ left: `${STICKY_COLUMN_LEFTS.contractNo}px`, width: `${STICKY_COLUMN_WIDTHS.contractNo}px` }}
                      >
                        <Link 
                          href={`/group/contracts/${encodeURIComponent(contract.cmpCd)}/${encodeURIComponent(contract.contractNo)}`}
                          onClick={() => handleDetailClick(contract.cmpCd, contract.contractNo)}
                          className="text-blue-600 hover:text-blue-800 hover:underline"
                          aria-label={`契約詳細へ移動: ${contract.contractNo}`}
                        >
                          {contract.contractNo}
                        </Link>
                      </td>
                      <td 
                        className="px-4 py-2 text-sm sticky bg-white group-hover:bg-gray-50 z-20"
                        style={{ left: `${STICKY_COLUMN_LEFTS.familyNameGaiji}px`, width: `${STICKY_COLUMN_WIDTHS.familyNameGaiji}px` }}
                        title={nameTitle}
                      >
                        <div className="leading-tight">
                          <div>{nameKanji || '-'}</div>
                          {nameKana && (
                            <div className="text-xs text-gray-500 mt-0.5">{nameKana}</div>
                          )}
                        </div>
                      </td>
                      <td 
                        className="px-4 py-2 text-sm sticky bg-white group-hover:bg-gray-50 z-20"
                        style={{ left: `${STICKY_COLUMN_LEFTS.tel}px`, width: `${STICKY_COLUMN_WIDTHS.tel}px` }}
                      >
                        <div className="leading-tight text-xs">
                          <div>{telNo}</div>
                          <div className="mt-0.5">{mobileNo}</div>
                        </div>
                      </td>
                      {/* 横スクロール対象列 */}
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('contractReceiptYmd') ? '' : 'hidden'}`}>
                        {formatDate(contract.contractReceiptYmd)}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('birthday') ? '' : 'hidden'}`}>
                        {formatDate(contract.birthday)}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('contractStatus') ? '' : 'hidden'}`}>
                        {contract.contractStatus ?? '-'}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('courseName') ? '' : 'hidden'}`} title={contract.courseName || contract.courseCd || undefined}>
                        {truncateText(contract.courseName || contract.courseCd, 20)}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('bosyuStaff') ? '' : 'hidden'}`} title={bosyuTitle}>
                        {displayBosyuName}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('entryStaff') ? '' : 'hidden'}`} title={entryTitle}>
                        {displayEntryName}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('address') ? '' : 'hidden'}`} title={addressTitle}>
                        {displayAddress}
                      </td>
                      <td className={`px-4 py-2 text-right text-sm ${visibleColumns.has('shareNum') ? '' : 'hidden'}`}>
                        {formatNumber(contract.shareNum)}
                      </td>
                      <td className={`px-4 py-2 text-right text-sm ${visibleColumns.has('monthlyPremium') ? '' : 'hidden'}`}>
                        {formatAmount(contract.monthlyPremium)}
                      </td>
                      <td className={`px-4 py-2 text-right text-sm ${visibleColumns.has('contractGaku') ? '' : 'hidden'}`}>
                        {formatAmount(contract.contractGaku)}
                      </td>
                      <td className={`px-4 py-2 text-right text-sm ${visibleColumns.has('totalSaveNum') ? '' : 'hidden'}`}>
                        {formatNumber(contract.totalSaveNum)}
                      </td>
                      <td className={`px-4 py-2 text-right text-sm ${visibleColumns.has('totalGaku') ? '' : 'hidden'}`}>
                        {formatAmount(contract.totalGaku)}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('motoSupplyRankOrgCd') ? '' : 'hidden'}`}>
                        {contract.motoSupplyRankOrgCd ?? '-'}
                      </td>
                      <td className={`px-4 py-2 text-sm ${visibleColumns.has('motoSupplyRankOrgName') ? '' : 'hidden'}`} title={contract.motoSupplyRankOrgName || undefined}>
                        {truncateText(contract.motoSupplyRankOrgName, 20)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="p-4 border-t border-gray-200 flex items-center justify-between flex-shrink-0">
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