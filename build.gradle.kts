plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

/*
subprojects {
    tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
        compilerOptions {
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                arrayOf("reports", "metrics").forEach {
                    freeCompilerArgs.addAll(
                        listOf(
                            "-P",
                            "plugin:androidx.compose.compiler.plugins.kotlin:${it}Destination=${layout.buildDirectory.asFile.get().absolutePath}/compose_metrics"
                        )
                    )
                }
            }
        }
    }
    /*
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                arrayOf("reports", "metrics").forEach {
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:${it}Destination=${layout.buildDirectory.asFile.get().absolutePath}/compose_metrics"
                    )
                }
            }
        }
    }
     */
}
*/
