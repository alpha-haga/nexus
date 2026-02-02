/**
 * Person API サービス
 *
 * identity モジュール（人物管理）のAPI呼び出し
 */

import { apiClient } from './api';
import type {
  Person,
  PersonId,
  RegisterPersonRequest,
  UpdatePersonRequest,
  Region,
} from '@/types';

export const personService = {
  /**
   * 人物を登録
   */
  async register(region: NonNullable<Region>, request: RegisterPersonRequest): Promise<Person> {
    return apiClient.post<Person>('/persons', request, region);
  },

  /**
   * 人物を取得
   */
  async findById(region: NonNullable<Region>, personId: PersonId): Promise<Person> {
    return apiClient.get<Person>(`/persons/${personId}`, region);
  },

  /**
   * 人物を検索
   */
  async search(region: NonNullable<Region>, keyword: string): Promise<Person[]> {
    return apiClient.get<Person[]>(
      `/persons?keyword=${encodeURIComponent(keyword)}`,
      region
    );
  },

  /**
   * 人物を更新
   */
  async update(
    region: NonNullable<Region>,
    personId: PersonId,
    request: UpdatePersonRequest
  ): Promise<Person> {
    return apiClient.patch<Person>(`/persons/${personId}`, request, region);
  },
};
