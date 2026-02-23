import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.apply
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    id("com.vanniktech.maven.publish")
    id("maven-publish")
}

kotlin {
    androidLibrary {
        namespace = "com.kyant.backdrop"
        compileSdk = 36
        buildToolsVersion = "36.1.0"
        minSdk = 21

        withJava()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.compose.ui.graphics)
        }
        val skikoMain by creating {
            dependsOn(commonMain.get())
        }
        iosMain.get().dependsOn(skikoMain)
        jvmMain.get().dependsOn(skikoMain)
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xexpect-actual-classes"
        )
    }
}
group = "io.lapockett"
version = "1.2.0"

/**
 * - Para GithubPackages
 * Para obtener el username y el password de properties
 */

val secretsFile: File = rootProject.file("local.properties")
if (secretsFile.exists()) {
    val secretsProps = Properties().apply { load(secretsFile.inputStream()) }
    secretsProps.forEach { (key, value) ->
        project.extensions.extraProperties[key.toString()] = value
    }
}
val gprKey: String? = project.findProperty("gpr.key") as? String ?: System.getenv("TOKEN")
val ghName: String? = project.findProperty("username") as? String ?: System.getenv("USERNAME")
publishing {
    publications {
        create<MavenPublication>("gpr") {
            groupId = groupId
            artifactId = "cmpglass"
            version = version

            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/LaPockett/ui-logo")
            credentials {
                username = ghName
                password = gprKey
            }
        }
    }
}