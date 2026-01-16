package nexus.gojo.contract.repository

import nexus.core.id.GojoContractId
import nexus.core.pagination.PaginatedResult
import nexus.core.id.PersonId
import nexus.gojo.contract.entity.Contract

/**
 * 互助会契約リポジトリ（インターフェース）
 *
 * 実装は infrastructure 層に配置（GojoContractRepositoryImpl）
 * domain 層は JDBC / SQL を知らない
 */
interface GojoContractRepository {

    fun findById(contractId: GojoContractId, regionId: String): Contract?

    fun findByContractorPersonId(personId: PersonId, regionId: String): List<Contract>

    fun save(contract: Contract, regionId: String): Contract

    fun updateStatus(contractId: GojoContractId, newStatus: String, regionId: String): Int

    /**
     * 地区内の契約をページネーションで取得
     *
     * @param regionId 地区ID（必須）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ（20, 50, 100のみ）
     * @return ページネーション結果
     */
    fun findByRegion(regionId: String, page: Int, size: Int): PaginatedResult<Contract>

    /**
     * 地区内の複数法人スキーマ横断で契約をページネーションで取得
     *
     * @param regionId 地区ID（必須）
     * @param corporationId 法人ID（任意、null の場合は全法人）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ（20, 50, 100のみ）
     * @return ページネーション結果
     */
    fun findAll(regionId: String, corporationId: String?, page: Int, size: Int): PaginatedResult<Contract>
}