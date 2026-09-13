plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

val gdxVersion = property("gdxVersion") as String

dependencies {
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
}

kotlin {
    jvmToolchain(17)
}
