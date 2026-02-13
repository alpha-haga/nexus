/**
 * NEXUS API クライアント
 *
 * バックエンドAPIとの通信を担当
 * 各モジュールからはこのサービスを経由してAPIを呼び出す
 *
 * 注意: この ApiClient は Client Component からのみ呼び出す前提とする。
 * Server Component から呼び出す場合は、server用クライアントを別途作成する（P2-2以降で対応）。
 */

import type { ApiError, Region } from '@/types';
import { getSession, signOut } from 'next-auth/react';
import { getSavedTenant } from '@/modules/core/utils/tenantStorage';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {},
    region: Region | null
  ): Promise<T> {
    // Region 未指定の場合は ApiError(400) を throw
    if (!region) {
      const error: ApiError = {
        timestamp: new Date().toISOString(),
        status: 400,
        error: 'Bad Request',
        message: 'Region が指定されていません。Region を選択してください。',
        code: 'REGION_REQUIRED',
      };
      throw error;
    }

    const url = `${this.baseUrl}${endpoint}`;

    // NextAuth.jsのセッションからaccessTokenを取得
    const session = await getSession();
    const accessToken = session?.accessToken;

    // HeadersInit を Record<string, string> に正規化
    const headersObj: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    const h = options.headers;
    if (h) {
      if (h instanceof Headers) {
        h.forEach((value, key) => {
          headersObj[key] = value;
        });
      } else if (Array.isArray(h)) {
        for (const [key, value] of h) {
          headersObj[key] = value;
        }
      } else {
        Object.assign(headersObj, h);
      }
    }

    // AuthorizationヘッダーにBearer Tokenを付与
    if (accessToken) {
      headersObj['Authorization'] = `Bearer ${accessToken}`;
    }

    // X-NEXUS-REGION ヘッダーを付与
    headersObj['X-NEXUS-REGION'] = region;

    // X-Company-Code ヘッダーを付与（全APIで送る）
    // sessionStorageから読む（TenantContextが保存した値）
    const cmpCd = getSavedTenant();
    if (cmpCd) {
      headersObj['X-Company-Code'] = cmpCd;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers: headersObj,
      });

      if (!response.ok) {
        // 401 Unauthorizedの場合は認証エラーとして扱う
        if (response.status === 401) {
          // NextAuth.jsのsignOutを呼び出してログイン画面にリダイレクト
          await signOut({ callbackUrl: '/login' });
          const error: ApiError = {
            timestamp: new Date().toISOString(),
            status: 401,
            error: 'Unauthorized',
            message: '認証に失敗しました。再度ログインしてください。',
            code: 'UNAUTHORIZED',
          };
          throw error;
        }

        // 403 Forbidden の場合は権限エラー
        if (response.status === 403) {
          const error: ApiError = {
            timestamp: new Date().toISOString(),
            status: 403,
            error: 'Forbidden',
            message: 'この法人へのアクセス権限がありません',
            code: 'ACCESS_DENIED',
          };
          throw error;
        }

        // 503 Service Unavailable の場合は法人利用不可
        if (response.status === 503) {
          const error: ApiError = {
            timestamp: new Date().toISOString(),
            status: 503,
            error: 'Service Unavailable',
            message: 'この法人は現在利用できません',
            code: 'COMPANY_NOT_AVAILABLE',
          };
          throw error;
        }

        // エラーレスポンス本文を読み取り
        let errorBody: ApiError;
        try {
          const json = await response.json();
          errorBody = {
            timestamp: json.timestamp || new Date().toISOString(),
            status: json.status || response.status,
            error: json.error || response.statusText,
            message: json.message || 'An error occurred',
            code: json.code,
            correlationId: json.correlationId,
          };
        } catch {
          // JSON が読めない場合は最低限の情報で組み立て
          errorBody = {
            timestamp: new Date().toISOString(),
            status: response.status,
            error: response.statusText,
            message: 'An error occurred',
            code: 'UNKNOWN_ERROR',
          };
        }
        throw errorBody;
      }

      // 204 No Content の場合は undefined を返す
      if (response.status === 204) {
        return undefined as T;
      }

      return response.json();
    } catch (err) {
      // fetch失敗等の例外も ApiError(500) に変換
      if (err && typeof err === 'object' && 'status' in err) {
        // 既に ApiError の場合はそのまま throw
        throw err;
      }
      const error: ApiError = {
        timestamp: new Date().toISOString(),
        status: 500,
        error: 'Internal Server Error',
        message: err instanceof Error ? err.message : 'ネットワークエラーが発生しました',
        code: 'NETWORK_ERROR',
      };
      throw error;
    }
  }

  async get<T>(endpoint: string, region: Region | null): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' }, region);
  }

  async post<T>(endpoint: string, body: unknown, region: Region | null): Promise<T> {
    return this.request<T>(
      endpoint,
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      region
    );
  }

  async patch<T>(endpoint: string, body: unknown, region: Region | null): Promise<T> {
    return this.request<T>(
      endpoint,
      {
        method: 'PATCH',
        body: JSON.stringify(body),
      },
      region
    );
  }

  async put<T>(endpoint: string, body: unknown, region: Region | null): Promise<T> {
    return this.request<T>(
      endpoint,
      {
        method: 'PUT',
        body: JSON.stringify(body),
      },
      region
    );
  }

  async delete<T>(endpoint: string, region: Region | null): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' }, region);
  }
}

export const apiClient = new ApiClient(API_BASE_URL);