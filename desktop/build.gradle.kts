import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    named("main") {
        // Android surumuyle ortak kod (ceviriler, arayuz, animasyon, model)
        java.srcDirs("src", "../shared/src")
        resources.srcDirs("resources")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
}

compose.desktop {
    application {
        mainClass = "com.cybergah.securewipe.desktop.MainKt"

        nativeDistributions {
            // Windows'ta Msi/Exe, macOS'ta Dmg, Linux'ta Deb uretilir.
            // jpackage capraz derleme yapamaz: her paket kendi isletim sisteminde uretilir.
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Deb)

            packageName = "Besop"
            packageVersion = "1.3.0"
            description = "Bêşop - iz birakmadan siler / Secure Erase"
            vendor = "CyberGah"

            windows {
                menuGroup = "CyberGah"
                perUserInstall = true
                // Guncellemelerin ayni uygulamanin uzerine kurulmasi icin sabit UUID
                upgradeUuid = "7f3a1c9e-2b64-4d5a-9c11-8e0d5f6a3b21"
            }

            macOS {
                bundleID = "com.cybergah.securewipe"
                dockName = "Bêşop"
            }

            linux {
                packageName = "besop"
            }
        }
    }
}

// Gelistirme dogrulamasi: ./gradlew :desktop:selftest
tasks.register<JavaExec>("selftest") {
    group = "verification"
    mainClass.set("com.cybergah.securewipe.desktop.SelfTest")
    classpath = sourceSets["main"].runtimeClasspath
    defaultCharacterEncoding = "UTF-8"
    jvmArgs("-Dstdout.encoding=UTF-8", "-Dfile.encoding=UTF-8")
}
