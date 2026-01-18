plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-infrastructure"))

    // 基盤ドメイン（社内UIでも参照し得る）
    implementation(project(":nexus-group"))
    implementation(project(":nexus-identity"))
    implementation(project(":nexus-household"))

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // DB Driver（ローカル検証用の暫定。業務DBは OCI Oracle）
    runtimeOnly("com.h2database:h2")
}

tasks.bootJar {
    archiveBaseName.set("nexus-bff")
}