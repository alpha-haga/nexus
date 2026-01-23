package nexus.infrastructure.group.query

import nexus.core.id.CorporationId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.group.query.GroupContractDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractDto の RowMapper
 *
 * P04-1-2: JDBC RowMapper 完成
 * - ResultSet から GroupContractDto を構築
 * - ID は core の Value Object を生成
 * - LocalDate など型変換も行う
 */
class GroupContractRowMapper : RowMapper<GroupContractDto> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractDto =
        GroupContractDto(
            id = GojoContractId(
                requireNotNull(rs.getString("contract_id")) {
                    "contract_id is null (row=$rowNum)"
                }
            ),
            corporationId = CorporationId(
                requireNotNull(rs.getString("corporation_id")) {
                    "corporation_id is null (contract_id=${rs.getString("contract_id")})"
                }
            ),
            contractorPersonId = PersonId(
                requireNotNull(rs.getString("contractor_person_id")) {
                    "contractor_person_id is null (contract_id=${rs.getString("contract_id")})"
                }
            ),
            beneficiaryPersonId = rs.getString("beneficiary_person_id")?.let { PersonId(it) },

            planCode = requireNotNull(rs.getString("plan_code")) {
                "plan_code is null (contract_id=${rs.getString("contract_id")})"
            },
            planName = requireNotNull(rs.getString("plan_name")) {
                "plan_name is null (contract_id=${rs.getString("contract_id")})"
            },
            monthlyFee = rs.getLong("monthly_fee").also {
                if (rs.wasNull()) {
                    error("monthly_fee is null (contract_id=${rs.getString("contract_id")})")
                }
            },
            maturityAmount = rs.getLong("maturity_amount").also {
                if (rs.wasNull()) {
                    error("maturity_amount is null (contract_id=${rs.getString("contract_id")})")
                }
            },
            contractDate = requireNotNull(rs.getDate("contract_date")) {
                "contract_date is null (contract_id=${rs.getString("contract_id")})"
            }.toLocalDate(),
            maturityDate = rs.getDate("maturity_date")?.toLocalDate(),
            status = requireNotNull(rs.getString("status")) {
                "status is null (contract_id=${rs.getString("contract_id")})"
            }            
        )
}