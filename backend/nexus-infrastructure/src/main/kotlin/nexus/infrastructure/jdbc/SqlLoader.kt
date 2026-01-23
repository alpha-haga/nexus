package nexus.infrastructure.jdbc

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

/**
 * SQL Loader
 *
 * P0-3d-2 / P04:
 * - resources/sql 配下の SQL をクラスパスから読み込む
 * - 呼び出し側は "group/xxx.sql" のように指定
 * - 実際のパスは "sql/{path}"
 */
@Component
class SqlLoader {

    fun load(path: String): String {
        val resourcePath = "sql/$path"
        val resource = ClassPathResource(resourcePath)

        require(resource.exists()) {
            "SQL resource not found: $resourcePath"
        }

        return resource.inputStream.use {
            it.readBytes().toString(StandardCharsets.UTF_8)
        }
    }
}
