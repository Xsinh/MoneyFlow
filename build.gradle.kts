buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.android.tools.build:gradle:7.0.0-alpha03")
        classpath(Deps.SqlDelight.gradle)
        classpath("com.github.ben-manes:gradle-versions-plugin:0.36.0")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://dl.bintray.com/ekito/koin" )
    }
}


group = "com.prof18"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
