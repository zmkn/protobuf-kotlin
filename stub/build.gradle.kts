plugins {
    alias(libs.plugins.protobuf)
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

dependencies {
    protobuf(project(":protobuf"))
    api(libs.protobuf.java.util)
    api(libs.protobuf.kotlin)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}
