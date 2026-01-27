import org.springframework.boot.gradle.tasks.run.BootRun
import java.io.File

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-infrastructure"))

    // 基盤ドメイン（社内UIでも参照し得る）
    implementation(project(":nexus-gojo"))
    implementation(project(":nexus-group"))
    implementation(project(":nexus-identity"))
    implementation(project(":nexus-household"))

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Security (P1-1: Keycloak claim based authorization in BFF)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // DB Driver（ローカル検証用の暫定。業務DBは OCI Oracle）
    runtimeOnly("com.h2database:h2")

    // ArchUnit
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}

// ====================
// .env 読み込み処理（bootRun 専用）
// ====================
fun loadDotEnv(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()

    return file.readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map { line ->
            val normalized = line.removePrefix("export ").trim()
            val idx = normalized.indexOf("=")
            if (idx <= 0) return@map null
            val key = normalized.substring(0, idx).trim()
            var value = normalized.substring(idx + 1).trim()

            // remove surrounding quotes
            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length - 1)
            }
            key to value
        }
        .filterNotNull()
        .toMap()
}

tasks.named<BootRun>("bootRun") {
    // backend/ で実行する前提: backend/.env を読む
    val dotenvFile = project.rootProject.file(".env")
    val dotenv = loadDotEnv(dotenvFile)

    // 既にOS側で設定済みの環境変数は上書きしない
    val merged = dotenv.filterKeys { System.getenv(it) == null }

    if (merged.isNotEmpty()) {
        environment(merged)
        println("[bootRun] injected .env vars: ${merged.keys.sorted().joinToString(", ")}")
    } else {
        println("[bootRun] no .env vars injected (file missing or all vars already set)")
    }
}