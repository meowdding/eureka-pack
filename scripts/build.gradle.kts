@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.collections.emptyList
import kotlin.io.path.absolutePathString

plugins {
    idea
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
    application
}

sourceSets {
    main {
        kotlin {
            srcDirs(listOf(layout.projectDirectory.dir("scripts")))
        }
        resources {
            setSrcDirs(emptyList<Any>())
        }
        java {
            setSrcDirs(emptyList<Any>())
        }
    }
}

repositories {
    fun scopedMaven(url: String, vararg paths: String) = maven(url) { content { paths.forEach(::includeGroupAndSubgroups) } }

    scopedMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "me.djtheredstoner")
    scopedMaven("https://repo.hypixel.net/repository/Hypixel", "net.hypixel")
    scopedMaven("https://maven.parchmentmc.org/", "org.parchmentmc")
    scopedMaven("https://api.modrinth.com/maven", "maven.modrinth")
    scopedMaven("https://maven.teamresourceful.com/repository/maven-public/", "tech.thatgravyboat", "me.owdding")
    scopedMaven("https://maven.nucleoid.xyz/", "eu.pb4")
    scopedMaven("https://maven.fabricmc.net/", "net.fabricmc")
    scopedMaven("https://raw.githubusercontent.com/fishstiz/maven/m2", "io.github.fishstiz")
    scopedMaven("https://maven.operationpotato.com/snapshots", "com.operationpotato")
    scopedMaven("https://maven.operationpotato.com/releases", "com.operationpotato")
    maven("https://libraries.minecraft.net")
    maven(file(rootProject.projectDir.resolve("catharsis/.gradle/loom-cache/minecraftMaven")))
    maven(file(rootProject.projectDir.resolve("catharsis/.gradle/loom-cache/remapped_mods")))
    mavenCentral()
    mavenLocal()
}

repositories

/*
val latest = ":catharsis:1.21.11"
*/
tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        "-Xallow-any-scripts-in-source-roots",
        "-Xcontext-parameters"
    )

}

val version = "26.2"

dependencies {
    runtimeOnly(project(":catharsis:$version", configuration = "fat"))
    compileOnly(project(":catharsis:$version", configuration = "fat"))
    implementation(project(":catharsis:$version", configuration = "unobfuscatedBuild"))
}


ksp {
    arg("meowdding.project_name", "eureka")
    arg("meowdding.package", "eureka")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin.jvmToolchain(25)

application {
    mainClass = "UtilsKt"
}

tasks.named<JavaExec>("run") {
    workingDir = project.projectDir.parentFile
    jvmArgs("-Dcatharsis.skip-listeners")
}
