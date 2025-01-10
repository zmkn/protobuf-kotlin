import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import org.jreleaser.model.Active
import org.jreleaser.model.Signing
import org.jreleaser.model.Stereotype
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.jreleaser)
}

val localProperties = Properties()
localProperties.load(project.file("local.properties").inputStream())

group = "com.zmkn.protobuf"
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            from(components["java"])
            pom {
                name.set("Kotlin Protobuf")
                description.set("Basic Protobuf from Zmkn")
                url.set("https://github.com/zmkn/protobuf-kotlin")
                inceptionYear.set("2025")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/zmkn/protobuf-kotlin/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("zmkn")
                        name.set("HZ")
                        email.set("beijingren@vip.qq.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/zmkn/protobuf-kotlin.git")
                    developerConnection.set("scm:git:git@github.com:zmkn/protobuf-kotlin.git")
                    url.set("https://github.com/zmkn/protobuf-kotlin")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy").get().asFile)
        }
    }
}

jreleaser {
    project {
        name.set("Kotlin Protobuf")

        // A short description (60 chars max).
        //  Only if configured distributions or announcers.
        description.set("Basic Protobuf from Zmkn")

        // A list of author names.
        //  Only if configured distributions or announcers.
        authors.addAll(listOf("HZ"))

        // A list of tags.
        tags.addAll(listOf("kotlin", "protobuf"))

        // List of maintainers.
        // Values are typically GitHub/GitLab usernames.
        maintainers.addAll(listOf("HZ"))

        // The stereotype of this project.
        // Supported values are [`NONE`, `CLI`, `DESKTOP`, `WEB`, `MOBILE`].
        // Defaults to `NONE`.
        stereotype.set(Stereotype.WEB)

        // The project's license.
        // It's recommended to use a valid SPDX identifier if the project is Open Source.
        // See https://spdx.org/licenses.
        //  Only if configured distributions or announcers.
        license.set("MIT")

        // The project's inception year.
        inceptionYear.set("2025")

        snapshot {
            // A regex to determine if the project version is snapshot
            pattern.set(".*-SNAPSHOT")

            // The value of the snapshot tag.
            // If undefined, will use `early-access`.
            label.set("${rootProject.version}")

            // Generate full changelog since last non-snapshot release.
            // Default is `false`.
            fullChangelog.set(false)
        }

        links {
            homepage.set("https://github.com/zmkn/protobuf-kotlin")
            documentation.set("https://github.com/zmkn/protobuf-kotlin/wiki")
            license.set("https://github.com/zmkn/protobuf-kotlin/blob/master/LICENSE")
            bugTracker.set("https://github.com/zmkn/protobuf-kotlin/issues")
            donation.set("https://github.com/zmkn/protobuf-kotlin")
        }
    }
    signing {
        // Enables or disables file signing.
        // Supported values are [`NEVER`, `ALWAYS`, `RELEASE`, `SNAPSHOT`].
        // Defaults to `NEVER`.
        active.set(Active.ALWAYS)

        // Generates an armored signature.
        // Defaults to `false`.
        armored.set(true)

        // Verify signature files.
        // If `false` then `publicKey` may be ommitted.
        // Defaults to `true`.
        verify.set(true)

        // How should GPG keys be handled.
        // Supported values are [`MEMORY`, `FILE`, `COMMAND`, `COSIGN`].
        // Defaults to `MEMORY`.
        mode.set(Signing.Mode.MEMORY)

        // The passphrase required to read secret keys.
        passphrase.set(localProperties.getProperty("signing.password"))

        // The public GPG (ascii armored) used to sign files and commits.
        // Required when mode = `MEMORY` || `FILE`.
        publicKey.set(localProperties.getProperty("signing.publicKey"))

        // The private GPG (ascii armored) used to sign files and commits.
        // Required when mode = `MEMORY` || `FILE`.
        secretKey.set(localProperties.getProperty("signing.secretKey"))

        // Sign files.
        // Defaults to `true`.
        files.set(true)

        // Sign distribution artifacts.
        // Defaults to `true`.
        artifacts.set(true)

        // Sign checksum files.
        // Defaults to `true`.
        checksums.set(true)
    }
    release {
        // Repo in which the release will be created.
        github {
            // Disables or enables publication to GitHub.
            // defaults to `true`.
            enabled.set(true)

            // Defines the connection timeout in seconds.
            // Defaults to `20`.
            connectTimeout.set(20)

            // Defines the read timeout in seconds.
            // Defaults to `60`.
            readTimeout.set(60)

            // The user or organization that owns the repository.
            repoOwner.set("zmkn")

            // The name of the repository.
            // If unspecified, will use `#{project.name}`.
            name.set(localProperties.getProperty("jreleaser.release.github.name"))

            // The GitHub host url.
            host.set("github.com")

            // Username used for authoring commits. Must have write access to the repository.
            // Defaults to the repository's owner.
            username.set(localProperties.getProperty("jreleaser.release.github.username"))

            // Password or OAuth token with write access to the repository.
            token.set(localProperties.getProperty("jreleaser.release.github.token"))

            // Drops and creates an existing release with matching tag.
            // Defaults to `false`.
            overwrite.set(true)

            update {
                // Appends artifacts to an existing release with matching tag,
                // useful if `overwrite` is set to `false`.
                // Defaults to `false`.
                enabled.set(true)

                // Release sections to be updated.
                // Supported values are [`TITLE`, `BODY`, `ASSETS`].
                // Defaults to `ASSETS`.
                section("ASSETS")
            }

            // Skips creating a tag.
            // Useful when the tag was created externally.
            // Defaults to `false`.
            skipTag.set(false)

            // Skips creating a release.
            // Useful when release assets will be handled with an uploader.
            // Defaults to `false`.
            skipRelease.set(false)

            // Signs commits with the configured credentials.
            // The Signing section must be configured as well.
            // Defaults to `false`.
            sign.set(true)

            // Git author used to commit to the repository.
            commitAuthor {
                // Name used when authoring commits.
                // Defaults to `jreleaserbot`.
                name.set("HZ")

                // E-mail used when authoring commits.
                // Defaults to `jreleaser@kordamp.org`.
                email.set("beijingren@vip.qq.com")
            }
        }
    }
    deploy {
        maven {
            github {
                create("github") {
                    // Enables or disables the deployer.
                    // Supported values are [`NEVER`, `ALWAYS`, `RELEASE`, `SNAPSHOT`].
                    // Defaults to `NEVER`.
                    active.set(Active.ALWAYS)

                    // URL where the Github service is enabled.
                    url.set("https://maven.pkg.github.com/zmkn/protobuf-kotlin")

                    // Activates publication of snapshot artifacts.
                    // Defaults to `false`.
                    snapshotSupported.set(true)

                    // The username required for authorization.
                    username.set(localProperties.getProperty("jreleaser.release.github.username"))

                    // Password for login into the GITHUB service.
                    password.set(localProperties.getProperty("jreleaser.release.github.token"))

                    // Signs artifacts with the configured credentials.
                    // The Signing section must be configured as well.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    sign.set(true)

                    // Checksums all artifacts with `MD5`, `SHA-1`, `SHA-256`, and `SHA-512`.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    checksums.set(true)

                    // Verifies that a matching `-sources.jar` artifact is staged.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    sourceJar.set(true)

                    // Verifies that a matching `-javadoc.jar` artifact is staged.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    javadocJar.set(true)

                    // Verifies that POM files comply with the minimum requirements for publication
                    // to Maven Central. Checks rules using PomChecker.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    verifyPom.set(true)

                    // Verifies pom files, signs all artifacts, verifies that matching `-sources.jar` and
                    // `-javadoc.jar` artifacts are also staged.
                    // Defaults to `false`.
                    applyMavenCentralRules.set(true)

                    // Override artifact configuration
                    artifactOverride {
                        // Match artifact by artifactId
                        artifactId.set(rootProject.project.name)

                        // Verifies that a matching `.jar` artifact is staged.
                        jar.set(true)

                        // Verifies that a matching `-sources.jar` artifact is staged.
                        sourceJar.set(true)

                        // Verifies that a matching `-javadoc.jar` artifact is staged.
                        javadocJar.set(true)

                        // Verifies that POM files comply with the minimum requirements for publication
                        // to Maven Central. Checks rules using PomChecker.
                        verifyPom.set(true)
                    }

                    // List of directories where staged artifacts can be found.
                    stagingRepository("build/staging-deploy")

                    // Defines the connection timeout in seconds.
                    // Defaults to `20`.
                    connectTimeout.set(20)

                    // Defines the read timeout in seconds.
                    // Defaults to `60`.
                    readTimeout.set(60)

                    // The name of the repository.
                    // If unspecified, will use `#{release.${releaser}.name}`.
                    repository.set(localProperties.getProperty("jreleaser.release.github.name"))
                }
            }
            mavenCentral {
                create("central") {
                    // Enables or disables the deployer.
                    // Supported values are [`NEVER`, `ALWAYS`, `RELEASE`, `SNAPSHOT`].
                    // Defaults to `NEVER`.
                    active.set(Active.ALWAYS)

                    // URL where the MavenCentral service is enabled.
                    url.set("https://central.sonatype.com/api/v1/publisher")

                    // Activates publication of snapshot artifacts.
                    // Defaults to `false`.
                    snapshotSupported.set(true)

                    // The username required for authorization.
                    username.set(localProperties.getProperty("jreleaser.deploy.maven.mavenCentral.username"))

                    // Password for login into the MAVENCENTRAL service.
                    password.set(localProperties.getProperty("jreleaser.deploy.maven.mavenCentral.password"))

                    // Signs artifacts with the configured credentials.
                    // The Signing section must be configured as well.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    sign.set(true)

                    // Checksums all artifacts with `MD5`, `SHA-1`, `SHA-256`, and `SHA-512`.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    checksums.set(true)

                    // Verifies that a matching `-sources.jar` artifact is staged.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    sourceJar.set(true)

                    // Verifies that a matching `-javadoc.jar` artifact is staged.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    javadocJar.set(true)

                    // Verifies that POM files comply with the minimum requirements for publication
                    // to Maven Central. Checks rules using PomChecker.
                    // Defaults to `false` unless `applyMavenCentralRules` is set to `true`.
                    verifyPom.set(true)

                    // Verifies pom files, signs all artifacts, verifies that matching `-sources.jar` and
                    // `-javadoc.jar` artifacts are also staged.
                    // Defaults to `false`.
                    applyMavenCentralRules.set(true)

                    // Override artifact configuration
                    artifactOverride {
                        // Match artifact by artifactId
                        artifactId.set(rootProject.project.name)

                        // Verifies that a matching `.jar` artifact is staged.
                        jar.set(true)

                        // Verifies that a matching `-sources.jar` artifact is staged.
                        sourceJar.set(true)

                        // Verifies that a matching `-javadoc.jar` artifact is staged.
                        javadocJar.set(true)

                        // Verifies that POM files comply with the minimum requirements for publication
                        // to Maven Central. Checks rules using PomChecker.
                        verifyPom.set(true)
                    }

                    // List of directories where staged artifacts can be found.
                    stagingRepository("build/staging-deploy")

                    // Defines the connection timeout in seconds.
                    // Defaults to `20`.
                    connectTimeout.set(20)

                    // Defines the read timeout in seconds.
                    // Defaults to `60`.
                    readTimeout.set(60)

                    // URL for checking artifacts may be already deployed.
                    // Additional template tokens: `groupId`, `artifactId`, `version`, `path`, `filename`.
                    verifyUrl.set("https://repo1.maven.org/maven2/{{path}}/{{filename}}")

                    // Time to wait between state transition checks, in seconds.
                    // Defaults to `10`.
                    retryDelay.set(10)

                    // Maximum number of attempts to verify state transition.
                    // Defaults to `60`.
                    maxRetries.set(60)
                }
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
