import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    id("org.springframework.boot") version "3.2.2" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

// ====================
// 全サブプロジェクト共通設定
// ====================
allprojects {
    group = "nexus"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// ====================
// サブプロジェクト共通設定
// ====================
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // Java/Kotlin バージョン設定
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        // 共通テスト依存
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.1")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }
}

// ====================
// Pure Kotlin モジュール（Spring依存禁止）
// ====================
val pureKotlinModules = listOf("nexus-core")

// ====================
// Spring Boot 依存モジュール
// ====================
val springModules = listOf(
    "nexus-group",
    "nexus-identity",
    "nexus-household",
    "nexus-gojo",
    "nexus-funeral",
    "nexus-bridal",
    "nexus-point",
    "nexus-agent",
    "nexus-payment",
    "nexus-accounting",
    "nexus-reporting",
    "nexus-api",
    "nexus-batch"
)

// ====================
// Spring Boot アプリケーションモジュール
// ====================
val bootApplicationModules = listOf("nexus-api", "nexus-batch")

configure(subprojects.filter { it.name in pureKotlinModules }) {
    // Pure Kotlin: Spring依存なし
    // nexus-coreはValueObject、ID、例外のみを持つ
}

configure(subprojects.filter { it.name in springModules }) {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.2")
        }
    }

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }
}

configure(subprojects.filter { it.name in bootApplicationModules }) {
    apply(plugin = "org.springframework.boot")

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-actuator")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    }
}
