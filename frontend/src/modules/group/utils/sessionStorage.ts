import type {
  Region,
  GroupContractSearchCondition,
  PaginatedGroupContractResponse,
} from '@/types';

type ColumnPreset = 'standard' | 'contact' | 'staff';

const STORAGE_KEYS = {
  lastSelectedKey: 'groupContracts:lastSelectedKey',
  lastScrollY: 'groupContracts:lastScrollY',
  lastQueryKey: 'groupContracts:lastQueryKey',
  lastCondition: 'groupContracts:lastCondition',
  lastResult: 'groupContracts:lastResult',
  lastRegion: 'groupContracts:lastRegion',
  lastPage: 'groupContracts:lastPage',
  lastSize: 'groupContracts:lastSize',
  lastColumnPreset: 'groupContracts:lastColumnPreset',
} as const;

/**
 * 検索条件+ページネーション+列プリセットから一意のキーを生成
 */
export function buildQueryKey(
  condition: GroupContractSearchCondition,
  page: number,
  size: number,
  columnPreset: ColumnPreset
): string {
  const normalized = {
    condition: {
      ...condition,
      // 配列はソートして比較
      cmpCds: condition.cmpCds ? [...condition.cmpCds].sort() : undefined,
    },
    page,
    size,
    columnPreset,
  };
  return JSON.stringify(normalized);
}

/**
 * 一覧状態を sessionStorage に保存
 */
export function saveListState(
  region: Region,
  condition: GroupContractSearchCondition,
  page: number,
  size: number,
  columnPreset: ColumnPreset,
  result: PaginatedGroupContractResponse,
  additional?: {
    selectedKey?: string;
    scrollY?: number;
  }
): void {
  if (typeof window === 'undefined') return;

  try {
    const queryKey = buildQueryKey(condition, page, size, columnPreset);
    
    sessionStorage.setItem(STORAGE_KEYS.lastCondition, JSON.stringify(condition));
    sessionStorage.setItem(STORAGE_KEYS.lastQueryKey, queryKey);
    sessionStorage.setItem(STORAGE_KEYS.lastResult, JSON.stringify(result));
    sessionStorage.setItem(STORAGE_KEYS.lastRegion, region);
    sessionStorage.setItem(STORAGE_KEYS.lastPage, page.toString());
    sessionStorage.setItem(STORAGE_KEYS.lastSize, size.toString());
    sessionStorage.setItem(STORAGE_KEYS.lastColumnPreset, columnPreset);

    if (additional?.selectedKey) {
      sessionStorage.setItem(STORAGE_KEYS.lastSelectedKey, additional.selectedKey);
    }
    if (additional?.scrollY !== undefined) {
      sessionStorage.setItem(STORAGE_KEYS.lastScrollY, additional.scrollY.toString());
    }
  } catch (e) {
    // sessionStorage が使用できない場合（プライベートモード等）は無視
    console.warn('Failed to save list state to sessionStorage:', e);
  }
}

/**
 * sessionStorage から保存された状態を取得（条件チェックなし）
 * 初回マウント時の復元に使用
 */
export function getSavedState(): {
  region?: Region;
  condition?: GroupContractSearchCondition;
  page?: number;
  size?: number;
  columnPreset?: ColumnPreset;
  result?: PaginatedGroupContractResponse;
  selectedKey?: string;
  scrollY?: number;
  queryKey?: string;
} | null {
  if (typeof window === 'undefined') return null;

  try {
    const lastQueryKey = sessionStorage.getItem(STORAGE_KEYS.lastQueryKey);
    const lastResult = sessionStorage.getItem(STORAGE_KEYS.lastResult);
    const lastCondition = sessionStorage.getItem(STORAGE_KEYS.lastCondition);
    const lastRegion = sessionStorage.getItem(STORAGE_KEYS.lastRegion);
    const lastSelectedKey = sessionStorage.getItem(STORAGE_KEYS.lastSelectedKey);
    const lastScrollY = sessionStorage.getItem(STORAGE_KEYS.lastScrollY);
    const lastPage = sessionStorage.getItem(STORAGE_KEYS.lastPage);
    const lastSize = sessionStorage.getItem(STORAGE_KEYS.lastSize);
    const lastColumnPreset = sessionStorage.getItem(STORAGE_KEYS.lastColumnPreset);

    if (!lastQueryKey || !lastResult || !lastRegion) {
      return null;
    }

    // lastCondition から condition を復元
    let condition: GroupContractSearchCondition | undefined;
    if (lastCondition) {
      try {
        condition = JSON.parse(lastCondition) as GroupContractSearchCondition;
      } catch {
        // パースエラーは無視
        console.warn('Failed to parse lastCondition from sessionStorage');
      }
    }

    const result = JSON.parse(lastResult) as PaginatedGroupContractResponse;

    return {
      region: lastRegion as Region,
      condition,
      result,
      selectedKey: lastSelectedKey ?? undefined,
      scrollY: lastScrollY ? Number.parseInt(lastScrollY, 10) : undefined,
      page: lastPage ? Number.parseInt(lastPage, 10) : undefined,
      size: lastSize ? (Number.parseInt(lastSize, 10) as 20 | 50 | 100) : undefined,
      columnPreset: lastColumnPreset as ColumnPreset | undefined,
      queryKey: lastQueryKey,
    };
  } catch (e) {
    console.warn('Failed to get saved state from sessionStorage:', e);
    return null;
  }
}

/**
 * sessionStorage から一覧状態を復元
 * 条件が一致する場合のみ復元する
 */
export function restoreListState(
  currentRegion: Region | null,
  currentCondition: GroupContractSearchCondition,
  currentPage: number,
  currentSize: number,
  currentColumnPreset: ColumnPreset
): {
  result?: PaginatedGroupContractResponse;
  selectedKey?: string;
  scrollY?: number;
  page?: number;
  size?: number;
  columnPreset?: ColumnPreset;
  queryKey?: string;
} | null {
  const saved = getSavedState();
  if (!saved || !saved.region || !saved.queryKey) {
    return null;
  }

  // Region が一致しない場合は復元しない
  if (saved.region !== currentRegion) {
    return null;
  }

  // 現在の条件と一致するか確認
  const currentQueryKey = buildQueryKey(
    currentCondition,
    currentPage,
    currentSize,
    currentColumnPreset
  );

  if (saved.queryKey !== currentQueryKey) {
    return null;
  }

  return {
    result: saved.result,
    selectedKey: saved.selectedKey,
    scrollY: saved.scrollY,
    page: saved.page,
    size: saved.size,
    columnPreset: saved.columnPreset,
    queryKey: saved.queryKey,
  };
}

/**
 * sessionStorage の一覧状態をクリア
 */
export function clearListState(): void {
  if (typeof window === 'undefined') return;

  try {
    Object.values(STORAGE_KEYS).forEach((key) => {
      sessionStorage.removeItem(key);
    });
  } catch (e) {
    console.warn('Failed to clear list state from sessionStorage:', e);
  }
}
