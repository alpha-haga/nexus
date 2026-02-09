import { apiClient } from './api';
import type {
  Company,
  GroupContractSearchCondition,
  GroupContractDetailResponse,
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
   * 法人マスタ一覧取得
   * Backend: GET /api/group/companies
   * 統合DBのため Region は INTEGRATION を指定
   */
  async getCompanies(): Promise<Company[]> {
    return apiClient.get<Company[]>(
      '/group/companies',
      'INTEGRATION'
    );
  },

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
    if (params.contractorName) {
      queryParams.append('contractorName', params.contractorName);
    }
    if (params.staffName) {
      queryParams.append('staffName', params.staffName);
    }
    if (params.cmpCds && params.cmpCds.length > 0) {
      // 配列を複数のクエリパラメータとして追加
      params.cmpCds.forEach((cd) => {
        queryParams.append('cmpCds', cd);
      });
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
    if (params.courseName) {
      queryParams.append('courseName', params.courseName);
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

  /**
   * 法人横断契約詳細取得
   * Backend: GET /api/v1/group/contracts/{cmpCd}/{contractNo}
   */
  async getContractDetail(
    cmpCd: string,
    contractNo: string
  ): Promise<GroupContractDetailResponse> {
    return apiClient.get<GroupContractDetailResponse>(
      `/group/contracts/${encodeURIComponent(cmpCd)}/${encodeURIComponent(contractNo)}`,
      'INTEGRATION'
    );
  },
};