// frontend/src/services/group.ts（新規作成）

import { apiClient } from './api';
import type {
  GroupContractSearchCondition,
  PaginatedGroupContractResponse,
  Region,
} from '@/types';

export interface SearchGroupContractsParams extends GroupContractSearchCondition {
  region: NonNullable<Region>;
  page: number;
  size: number;
}

export const groupService = {
  /**
   * 法人横断契約検索
   * Backend: GET /api/v1/group/contracts/search
   */
  async searchContracts(
    params: SearchGroupContractsParams
  ): Promise<PaginatedGroupContractResponse> {
    const queryParams = new URLSearchParams();
    
    // 検索条件（null/undefined は除外）
    if (params.contractReceiptYmdFrom) {
      queryParams.append('contractReceiptYmdFrom', params.contractReceiptYmdFrom);
    }
    if (params.contractReceiptYmdTo) {
      queryParams.append('contractReceiptYmdTo', params.contractReceiptYmdTo);
    }
    if (params.contractNo) {
      queryParams.append('contractNo', params.contractNo);
    }
    if (params.familyNmKana) {
      queryParams.append('familyNmKana', params.familyNmKana);
    }
    if (params.telNo) {
      queryParams.append('telNo', params.telNo);
    }
    if (params.bosyuCd) {
      queryParams.append('bosyuCd', params.bosyuCd);
    }
    if (params.courseCd) {
      queryParams.append('courseCd', params.courseCd);
    }
    if (params.contractStatusKbn) {
      queryParams.append('contractStatusKbn', params.contractStatusKbn);
    }
    
    // ページネーション（必須）
    queryParams.append('page', params.page.toString());
    queryParams.append('size', params.size.toString());
    
    const queryString = queryParams.toString();
    return apiClient.get<PaginatedGroupContractResponse>(
      `/group/contracts/search${queryString ? `?${queryString}` : ''}`,
      params.region
    );
  },
};