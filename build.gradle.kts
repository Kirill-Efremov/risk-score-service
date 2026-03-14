plugins {
    id("java")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "ru.kpfu.itis.efremov"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
repositories {
    mavenCentral()
}
dependencies {
    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql:42.7.3") // пример из гайда по Spring Boot + Postgres[web:472][web:474]

    // Avro
    implementation("org.apache.avro:avro:1.11.3")   // для SchemaCompatibility[web:353][web:484]

    // JSON Schema
    implementation("com.networknt:json-schema-validator:1.4.0")

    // Protobuf
    implementation("com.google.protobuf:protobuf-java:3.25.3")
    implementation("com.google.protobuf:protobuf-java-util:3.25.3")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Lombok (если хочешь)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Конфигурационные бинды
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Тесты
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


tasks.test {
    useJUnitPlatform()
}