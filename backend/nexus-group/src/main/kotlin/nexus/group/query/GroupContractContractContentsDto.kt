package nexus.group.query

/**
 * 契約内容（サブリソース）
 *
 * P2-5 までの「詳細（骨格）」は、基本情報/契約者/連絡先/住所/契約状態までを扱う。
 * 本 DTO は「契約内容」カードに対応する追加情報を取得するためのサブリソースである。
 *
 * P2-6：API 分離設計のための DTO 骨格。
 * P2-7 以降で項目を確定し、必要なフィールドを追加する。
 */
data class GroupContractContractContentsDto(
    val cmpCd: String,
    val contractNo: String,
    /**
     * ここで扱う項目は「骨格 API」に含めない。
     * 例：契約金額/積立情報/支払方法/更新履歴など。
     *
     * 項目が未確定のため、現時点では key-value 形式で保持する。
     * P2-7 以降で型付けする場合は、本 DTO の構造を固定したまま拡張する。
     */
    val attributes: Map<String, String?> = emptyMap(),
)
