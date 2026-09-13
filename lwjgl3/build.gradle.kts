plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

val gdxVersion = property("gdxVersion") as String

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

application {
    mainClass.set("com.korku.game3d.lwjgl3.Lwjgl3LauncherKt")
}

kotlin {
    jvmToolchain(17)
}

// Proje kökündeki assets/ klasörünü (müzik/sfx/diyalog seslendirme dosyaları
// dahil) hem `./gradlew lwjgl3:run` ile hem de IDE'den çalıştırırken
// Gdx.files.internal(...) çağrılarının bulabileceği şekilde ekliyoruz.
sourceSets {
    main {
        resources.srcDir(rootProject.file("assets"))
    }
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.file("assets")
    doFirst { workingDir.mkdirs() }
}
