import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.protobuf)
}

group = "com.ailingqi"
version = "1.0.0-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_22)
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                create("kotlin")
            }
        }
    }
}

val buildJreleaserDir = layout.buildDirectory.dir("jreleaser").get().asFile
if (!buildJreleaserDir.exists()) {
    buildJreleaserDir.mkdirs()
}

// 声明当前时间
val currentDateTime: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

tasks.withType<Jar> {
    archiveBaseName.set(project.name)
    archiveVersion.set("${project.version}.${currentDateTime}")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set(project.name)
    archiveClassifier.set("all") // 定义生成的 JAR 分类器名
    archiveVersion.set("${project.version}.${currentDateTime}")

    dependencies {
        exclude {
            it.moduleGroup == "org.jetbrains.kotlin"
        }
        exclude {
            it.moduleGroup == "org.jetbrains.kotlinx"
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    api(libs.protobuf.java.util)
    api(libs.protobuf.kotlin)
}
