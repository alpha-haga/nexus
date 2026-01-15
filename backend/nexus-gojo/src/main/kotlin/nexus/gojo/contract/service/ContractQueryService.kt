package nexus.gojo.contract.service

import nexus.core.exception.ValidationException
import nexus.gojo.contract.repository.GojoContractRepository
import nexus.gojo.contract.repository.PaginatedResult
import nexus.gojo.contract.entity.Contract
import org.springframework.stereotype.Service

/**
 * 互助会契約クエリサービス（読み取り専用）
 *
 * Read-only operations for contract queries
 */
@Service
class ContractQueryService(
    private val gojoContractRepository: GojoContractRepository
) {

    /**
     * 地区内の契約をページネーションで取得
     *
     * @param regionId 地区ID（必須）
     * @param page ページ番号（0始まり、必須）
     * @param size ページサイズ（20, 50, 100のみ、必須）
     * @return ページネーション結果
     */
    fun listLocalContracts(regionId: String, page: Int, size: Int): PaginatedResult<Contract> {
        // regionId バリデーション
        if (regionId.isBlank()) {
            throw ValidationException("regionId", "regionId is required")
        }

        // size バリデーション（20, 50, 100のみ）
        if (size !in listOf(20, 50, 100)) {
            throw ValidationException("size", "size must be one of: 20, 50, 100")
        }

        // page バリデーション
        if (page < 0) {
            throw ValidationException("page", "page must be >= 0")
        }

        return gojoContractRepository.findByRegion(regionId, page, size)
    }
}