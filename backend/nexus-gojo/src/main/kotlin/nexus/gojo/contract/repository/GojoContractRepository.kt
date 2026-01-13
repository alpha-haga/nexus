package nexus.gojo.contract.repository

import nexus.core.id.GojoContractId
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
}