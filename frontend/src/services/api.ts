/**
 * NEXUS API クライアント
 *
 * バックエンドAPIとの通信を担当
 * 各モジュールからはこのサービスを経由してAPIを呼び出す
 *
 * 注意: この ApiClient は Client Component からのみ呼び出す前提とする。
 * Server Component から呼び出す場合は、server用クライアントを別途作成する（P2-2以降で対応）。
 */

import type { ApiError } from '@/types';
import { getSession, signOut } from 'next-auth/react';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
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

    // 暫定: P2-1 は group の integration 想定
    headersObj['X-NEXUS-REGION'] = 'INTEGRATION';

    const response = await fetch(url, {
      ...options,
      headers: headersObj,
    });

    if (!response.ok) {
      // 401 Unauthorizedの場合は認証エラーとして扱う
      if (response.status === 401) {
        // NextAuth.jsのsignOutを呼び出してログイン画面にリダイレクト
        await signOut({ callbackUrl: '/login' });
        throw new Error('認証に失敗しました。再度ログインしてください。');
      }

      // 403 Forbiddenの場合はApiErrorとしてthrow（UI側で明示する）
      if (response.status === 403) {
        const error: ApiError = await response.json().catch(() => ({
          timestamp: new Date().toISOString(),
          status: 403,
          error: 'Forbidden',
          message: 'この操作を実行する権限がありません。',
          code: 'FORBIDDEN',
        }));
        throw error;
      }

      // その他のエラー
      const error: ApiError = await response.json().catch(() => ({
        timestamp: new Date().toISOString(),
        status: response.status,
        error: response.statusText,
        message: 'An error occurred',
        code: 'UNKNOWN_ERROR',
      }));
      throw error;
    }

    // 204 No Content の場合は undefined を返す
    if (response.status === 204) {
      return undefined as T;
    }

    return response.json();
  }

  async get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, body: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  async patch<T>(endpoint: string, body: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  }

  async put<T>(endpoint: string, body: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body),
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

export const apiClient = new ApiClient(API_BASE_URL);