buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.android.tools.build:gradle:7.1.0-alpha02")
        classpath(Deps.SqlDelight.gradle)
        classpath("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")

    }
}


group = "com.prof18"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
