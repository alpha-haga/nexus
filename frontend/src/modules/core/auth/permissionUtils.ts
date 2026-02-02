/**
 * 画面権限判定の純関数
 * 
 * React Hooks を使わずに権限判定を行う純関数
 * 判定ロジックを 1箇所に集約（if の増殖を防止）
 */

import type { Session } from 'next-auth';
import { SCREEN_PERMISSIONS, type ScreenKey } from './screenPermissions';

/**
 * 画面権限判定の結果
 */
export interface ScreenPermissionResult {
  /** アクセス可能か */
  canView: boolean;
  /** 拒否理由（canView === false の場合に設定） */
  reason?: string;
  /** ローディング中か */
  isLoading?: boolean;
  /** 認証済みか */
  isAuthenticated?: boolean;
}

/**
 * Session と status から画面権限を判定する純関数
 * 
 * @param session NextAuth の session（null の場合は未認証）
 * @param status NextAuth の status（'loading' | 'authenticated' | 'unauthenticated'）
 * @param screenKey 画面キー
 * @returns 画面権限判定の結果
 */
export function getScreenPermissionFromSession(
  session: Session | null,
  status: 'loading' | 'authenticated' | 'unauthenticated',
  screenKey: ScreenKey
): ScreenPermissionResult {
  // 認証状態確認中
  if (status === 'loading') {
    return {
      canView: false,
      reason: '認証状態確認中です',
      isLoading: true,
      isAuthenticated: false,
    };
  }

  // 未認証
  if (!session) {
    return {
      canView: false,
      reason: '未認証です',
      isLoading: false,
      isAuthenticated: false,
    };
  }

  // session.dbAccess が存在しない場合
  if (!session.dbAccess) {
    return {
      canView: false,
      reason: '権限情報が取得できませんでした。管理者に連絡してください。',
      isLoading: false,
      isAuthenticated: true,
    };
  }

  // 権限情報の取得エラーがある場合
  if (session.dbAccess.errors.length > 0) {
    return {
      canView: false,
      reason: `権限情報の取得に失敗しました: ${session.dbAccess.errors.join(', ')}`,
      isLoading: false,
      isAuthenticated: true,
    };
  }

  const permission = SCREEN_PERMISSIONS[screenKey];
  if (!permission) {
    return {
      canView: false,
      reason: '画面権限の定義が見つかりませんでした。',
      isLoading: false,
      isAuthenticated: true,
    };
  }

  // any: true の場合は全ユーザーがアクセス可能
  if (permission.any) {
    return {
      canView: true,
      isLoading: false,
      isAuthenticated: true,
    };
  }

  // denyAll: true の場合は全ユーザーに拒否
  if (permission.denyAll) {
    return {
      canView: false,
      reason: 'この画面は現在利用できません。',
      isLoading: false,
      isAuthenticated: true,
    };
  }

  // domain が指定されている場合
  if (permission.domain) {
    const requiredDomain = permission.domain;
    const roles = session.dbAccess.roles;

    // 必要な DomainAccount を持つ role が存在するか確認
    const hasRequiredDomain = roles.some(
      (role) => role.domainAccount === requiredDomain
    );

    if (!hasRequiredDomain) {
      return {
        canView: false,
        reason: `${requiredDomain} ドメインへのアクセス権限がありません。管理者に連絡してください。`,
        isLoading: false,
        isAuthenticated: true,
      };
    }

    // integrationOnly の場合は integration の role のみ許可
    if (permission.integrationOnly) {
      const hasIntegrationRole = roles.some(
        (role) => role.region.toLowerCase() === 'integration' && role.domainAccount === requiredDomain
      );

      if (!hasIntegrationRole) {
        return {
          canView: false,
          reason: '統合DB（integration）へのアクセス権限がありません。管理者に連絡してください。',
          isLoading: false,
          isAuthenticated: true,
        };
      }
    }

    return {
      canView: true,
      isLoading: false,
      isAuthenticated: true,
    };
  }

  // 上記のいずれにも該当しない場合は拒否
  return {
    canView: false,
    reason: 'アクセス権限がありません。管理者に連絡してください。',
    isLoading: false,
    isAuthenticated: true,
  };
}
