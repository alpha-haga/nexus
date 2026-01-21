package nexus.core.region

/**
 * NEXUS 地区定義
 *
 * ルーティングキーとして使用される固定の地区 enum
 */
enum class Region {
    /** 埼玉地区 */
    SAITAMA,

    /** 福島地区 */
    FUKUSHIMA,

    /** 栃木地区 */
    TOCHIGI,

    /** 統合DB */
    INTEGRATION;

    companion object {
        /**
         * 文字列から Region を取得（大小文字を許容）
         *
         * @param value 地区名（例: "saitama", "SAITAMA", "Saitama"）
         * @return 対応する Region、見つからない場合は null
         */
        fun fromString(value: String): Region? {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        /**
         * 文字列から Region を取得（大小文字を許容、失敗時は例外）
         *
         * @param value 地区名（例: "saitama", "SAITAMA", "Saitama"）
         * @return 対応する Region
         * @throws IllegalArgumentException 無効な地区名の場合
         */
        fun fromStringOrThrow(value: String): Region {
            return fromString(value)
                ?: throw IllegalArgumentException("Invalid region: $value. Valid values are: ${values().joinToString { it.name.lowercase() }}")
        }
    }
}
