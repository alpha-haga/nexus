/**
 * Tenant保存/復元ユーティリティ
 * 
 * 全ドメイン横断の基盤として、Tenant(cmpCd)の保存/復元を担当
 * 後方互換のため、旧キーも読み取り対象に含める
 */

const TENANT_STORAGE_KEY = 'nexus:tenant';
const LEGACY_KEYS = [
  'groupContracts:lastTenant',  // 旧キー
  'lastTenant',                  // 裸キー
] as const;

/**
 * sessionStorage から tenant を取得
 * 後方互換のため、旧キーも読み取り対象に含める
 */
export function getSavedTenant(): string | null {
  if (typeof window === 'undefined') return null;

  try {
    // 新キーを優先
    const tenant = sessionStorage.getItem(TENANT_STORAGE_KEY);
    if (tenant) {
      return tenant;
    }

    // 旧キーを読み取り（後方互換）
    for (const legacyKey of LEGACY_KEYS) {
      const legacyTenant = sessionStorage.getItem(legacyKey);
      if (legacyTenant) {
        // 見つかったら新キーにも保存して移行（互換クリーンアップ）
        sessionStorage.setItem(TENANT_STORAGE_KEY, legacyTenant);
        sessionStorage.removeItem(legacyKey); // 旧キー除去（移行完了）
        return legacyTenant;
      }
    }

    return null;
  } catch (e) {
    console.warn('Failed to get saved tenant from sessionStorage:', e);
    return null;
  }
}

/**
 * tenant を sessionStorage に保存
 */
export function saveTenant(tenant: string | null): void {
  if (typeof window === 'undefined') return;

  try {
    if (tenant) {
      sessionStorage.setItem(TENANT_STORAGE_KEY, tenant);
    } else {
      sessionStorage.removeItem(TENANT_STORAGE_KEY);
    }
  } catch (e) {
    console.warn('Failed to save tenant to sessionStorage:', e);
  }
}

/**
 * sessionStorage から tenant をクリア
 */
export function clearTenant(): void {
  if (typeof window === 'undefined') return;

  try {
    sessionStorage.removeItem(TENANT_STORAGE_KEY);
    // 旧キーもクリア（互換クリーンアップ）
    LEGACY_KEYS.forEach((key) => {
      sessionStorage.removeItem(key);
    });
  } catch (e) {
    console.warn('Failed to clear tenant from sessionStorage:', e);
  }
}
