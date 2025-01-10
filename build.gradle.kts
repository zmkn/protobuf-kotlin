import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow) apply false
}

allprojects {
    group = "com.ailingqi"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply {
        plugin(rootProject.libs.plugins.kotlin.jvm.get().pluginId)
        plugin(rootProject.libs.plugins.shadow.get().pluginId)
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(22))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_22)
        }
    }

    // 声明当前时间
    val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

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
}

tasks.register("build") {
    // 执行所有子项目的 build 任务
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
    dependsOn(subprojects.map { "${it.path}:build" })
}

tasks.register("clean") {
    // 执行所有子项目的 clean 任务
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
    dependsOn(subprojects.map { "${it.path}:clean" })
}
