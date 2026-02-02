/**
 * 画面権限判定 Hook
 * 
 * session.dbAccess.roles を参照して、画面へのアクセス権限を判定する
 * 判定ロジックは純関数（permissionUtils.ts）に集約
 */

'use client';

import { useSession } from 'next-auth/react';
import { useMemo } from 'react';
import type { ScreenKey } from './screenPermissions';
import { getScreenPermissionFromSession, type ScreenPermissionResult } from './permissionUtils';

/**
 * 画面権限を判定する Hook
 * 
 * @param screenKey 画面キー
 * @returns 画面権限判定の結果
 */
export function useScreenPermission(screenKey: ScreenKey): ScreenPermissionResult {
  const { data: session, status } = useSession();

  return useMemo(() => {
    return getScreenPermissionFromSession(session, status, screenKey);
  }, [session, status, screenKey]);
}
