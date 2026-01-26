package nexus.infrastructure.db

/**
 * DomainAccount（業務ドメイン別 DB 接続アカウント種別）
 *
 * 業務ドメインごとの接続アカウントを表す enum
 * - GOJO: nexus-gojo ドメイン用（実体スキーマ: XXX_gojo）
 * - FUNERAL: nexus-funeral ドメイン用（実体スキーマ: XXX_sousai）
 *
 * master は DomainAccount に内包（synonym 経由で参照可能）だが、直接接続対象にしない
 */
enum class DomainAccount {
    /** 互助会（nexus-gojo）用 - 実体スキーマ: XXX_gojo */
    GOJO,

    /** 葬祭（nexus-funeral）用 - 実体スキーマ: XXX_sousai */
    FUNERAL;

    companion object {
        /**
         * 文字列から DomainAccount を取得（大小文字を許容）
         *
         * @param value 業務名（例: "gojo", "GOJO", "funeral", "FUNERAL"）
         * @return 対応する DomainAccount、見つからない場合は null
         */
        fun fromString(value: String): DomainAccount? {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 文字列から DomainAccount を取得（大小文字を許容、失敗時は例外）
         *
         * @param value 業務名（例: "gojo", "GOJO", "funeral", "FUNERAL"）
         * @return 対応する DomainAccount
         * @throws IllegalArgumentException 無効な業務名の場合
         */
        fun fromStringOrThrow(value: String): DomainAccount {
            return fromString(value)
                ?: throw IllegalArgumentException("Invalid domainAccount: $value. Valid values are: ${values().joinToString { it.name }}")
        }
    }
}