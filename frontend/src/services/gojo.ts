/**
 * Gojo API サービス
 *
 * gojo モジュール（互助会契約）のAPI呼び出し
 */

import { apiClient } from './api';
import type { GojoContract, PaginatedResponse } from '@/types';

export interface ListLocalContractsParams {
  regionId: string;
  page: number;
  size: 20 | 50 | 100;
}

export const gojoService = {
  /**
   * 地区内の契約をページネーションで取得
   */
  async listLocal(params: ListLocalContractsParams): Promise<PaginatedResponse<GojoContract>> {
    const { regionId, page, size } = params;
    return apiClient.get<PaginatedResponse<GojoContract>>(
      `/gojo/contracts/local?regionId=${encodeURIComponent(regionId)}&page=${page}&size=${size}`
    );
  },
};