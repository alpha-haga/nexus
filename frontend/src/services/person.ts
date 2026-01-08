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
} from '@/types';

export const personService = {
  /**
   * 人物を登録
   */
  async register(request: RegisterPersonRequest): Promise<Person> {
    return apiClient.post<Person>('/persons', request);
  },

  /**
   * 人物を取得
   */
  async findById(personId: PersonId): Promise<Person> {
    return apiClient.get<Person>(`/persons/${personId}`);
  },

  /**
   * 人物を検索
   */
  async search(keyword: string): Promise<Person[]> {
    return apiClient.get<Person[]>(
      `/persons?keyword=${encodeURIComponent(keyword)}`
    );
  },

  /**
   * 人物を更新
   */
  async update(
    personId: PersonId,
    request: UpdatePersonRequest
  ): Promise<Person> {
    return apiClient.patch<Person>(`/persons/${personId}`, request);
  },
};
