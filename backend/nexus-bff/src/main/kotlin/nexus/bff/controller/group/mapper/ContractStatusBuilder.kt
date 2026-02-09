package nexus.bff.controller.group.mapper

import nexus.group.query.ContractStatusMaterials

/**
 * 契約状態表示文字列組み立てユーティリティ
 * 
 * 一覧・詳細の両方で使用する共通ロジック。
 * SQL CASE ロジックを 1:1 で写経した実装。
 */

/**
 * 状態文字列を結合する（片側 NULL でも表示）
 * 
 * - base と reason の両方が非null → "base（reason）"
 * - base のみ非null → base
 * - reason のみ非null → reason
 * - 両方 null → null
 */
internal fun joinStatus(base: String?, reason: String?): String? {
    return when {
        base != null && reason != null -> "$base（$reason）"
        base != null -> base
        reason != null -> reason
        else -> null
    }
}

/**
 * contract_status を組み立てる。
 *
 * 本ロジックは以下 SQL の CASE 式と 1:1 で一致させること。
 * - backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql
 * - backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_detail.sql
 *
 * SQL 側を変更した場合は、本メソッドも必ず同時に修正すること。
 * 一覧・詳細で表示不一致を起こさないための共通実装である。
 */
internal fun buildContractStatus(materials: ContractStatusMaterials): String? {
    val contractStatusKbn = materials.contractStatusKbn
    val contractStatusName = materials.contractStatusName
    
    return when (contractStatusKbn) {
        "1" -> contractStatusName
        "2" -> {
            when (materials.torikeshiReasonKbn) {
                "1" -> joinStatus(contractStatusName, materials.torikeshiReasonName)
                "2" -> contractStatusName
                "3" -> materials.torikeshiReasonName
                else -> null
            }
        }
        "3" -> {
            when {
                materials.anspApproveKbn == "1" -> {
                    when {
                        materials.dmdStopReasonKbn == "B" -> joinStatus(contractStatusName, materials.dmdStopReasonName)
                        else -> joinStatus(contractStatusName, materials.anspApproveName)
                    }
                }
                materials.anspApproveKbn == "2" -> joinStatus(contractStatusName, materials.anspApproveName)
                materials.ecApproveKbn == "1" -> {
                    when {
                        materials.dmdStopReasonKbn == "B" -> joinStatus(contractStatusName, materials.dmdStopReasonName)
                        else -> joinStatus(contractStatusName, materials.ecApproveName)
                    }
                }
                else -> {
                    when {
                        materials.dmdStopReasonName == null -> contractStatusName
                        else -> joinStatus(contractStatusName, materials.dmdStopReasonName)
                    }
                }
            }
        }
        "4" -> joinStatus(contractStatusName, materials.cancelReasonName)
        "5" -> joinStatus(contractStatusName, materials.zashuReasonName)
        "6" -> contractStatusName
        else -> null
    }
}
