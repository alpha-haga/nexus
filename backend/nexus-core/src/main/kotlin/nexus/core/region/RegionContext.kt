package nexus.core.region

import nexus.core.exception.RegionContextNotSetException

/**
 * RegionContext
 *
 * 現在の処理がどの Region DB に向くべきかを保持する ThreadLocal コンテキスト
 *
 * 責務:
 * - app 層（bff/api/batch）が set/clear する
 * - infrastructure 層（DataSource 層）が読む
 *
 * 実装方式:
 * - ThreadLocal を採用（P0段階の最小・堅牢案）
 * - app 層は必ず finally で clear する（リーク防止）
 *
 * 未設定時の挙動:
 * - get() が未設定なら例外を投げる（fail fast、事故防止）
 */
object RegionContext {
    private val context = ThreadLocal<Region>()

    /**
     * 現在の Region を設定
     *
     * @param region 設定する Region（null 禁止）
     */
    fun set(region: Region) {
        context.set(region)
    }

    /**
     * 現在の Region を取得
     *
     * @return 現在の Region
     * @throws RegionContextNotSetException Region が未設定の場合
     */
    fun get(): Region {
        return context.get() ?: throw RegionContextNotSetException()
    }

    /**
     * 現在の Region を取得（未設定時は null）
     */
    fun getOrNull(): Region? = context.get()

    /**
     * Region が設定されているか確認
     */
    fun isSet(): Boolean = context.get() != null

    /**
     * 現在の Region をクリア（リーク防止）
     */
    fun clear() {
        context.remove()
    }
}
