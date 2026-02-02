/**
 * 画面権限マッピング定義
 * 
 * 画面/ルート単位の最小権限マッピングを定義
 * 参照: docs/architecture/p2-3-frontend-authorization-roadmap.md の「3.6 ルート→必要権限（DomainAccount）マッピング表」
 * 
 * 重要: このマッピング表は「フロント独自の権限ロジックを増殖させない」ための唯一の固定点
 */

import type { DomainAccount } from '@/services/auth/dbAccess';

/**
 * 画面権限の定義
 */
export interface ScreenPermission {
  /** 全ユーザーがアクセス可能（認証済みであれば） */
  any?: boolean;
  /** 必要な DomainAccount */
  domain?: DomainAccount;
  /** integration 専用（GROUP の場合のみ） */
  integrationOnly?: boolean;
  /** 全ユーザーに拒否（P2-3 では未実装画面） */
  denyAll?: boolean;
}

/**
 * 画面キー（screenKey）の型
 */
export type ScreenKey =
  | 'dashboard'
  | 'gojo'
  | 'funeral'
  | 'groupContracts'
  | 'groupPersons'
  | 'points'
  | 'customers'
  | 'billing'
  | 'agents'
  | 'reports'
  | 'admin';

/**
 * 画面権限マッピング表
 * 
 * P2-3 では GOJO / FUNERAL / GROUP のみを扱う
 * POINTS は P2-3 では扱わない（非表示 or disabled + 理由表示）
 */
export const SCREEN_PERMISSIONS: Record<ScreenKey, ScreenPermission> = {
  dashboard: { any: true },
  
  gojo: { domain: 'GOJO' },
  funeral: { domain: 'FUNERAL' },
  
  groupContracts: {
    domain: 'GROUP',
    integrationOnly: true,
  },
  groupPersons: {
    domain: 'GROUP',
    integrationOnly: true,
  },
  
  // POINTS は P2-3 では扱わない（非表示 or disabled + 理由表示）
  points: { denyAll: true },
  
  // 暫定項目（P2-3 では扱わない）
  customers: { denyAll: true },
  billing: { denyAll: true },
  agents: { denyAll: true },
  reports: { denyAll: true },
  admin: { denyAll: true },
};
