/* Copyright (C) 2019 Jonas Herzig <me@johni0702.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("jvm") version("1.9.0")
    `kotlin-dsl`
    `maven-publish`
    groovy
}

group = "com.github.null2264"
version = "SNAPSHOT"

val kotestVersion: String by project.extra

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://jitpack.io/")
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.deftu.xyz/releases/")
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.github.replaymod:Remap:0.1.1")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
}

gradlePlugin {
    plugins {
        register("preprocess") {
            id = "com.github.null2264.preprocess"
            implementationClass = "com.replaymod.gradle.preprocess.PreprocessPlugin"
        }

        register("preprocess-root") {
            id = "com.github.null2264.preprocess-root"
            implementationClass = "com.replaymod.gradle.preprocess.RootPreprocessPlugin"
        }
    }
}

publishing {
    val publishingUsername: String? = run {
        return@run project.findProperty("deftu.publishing.username")?.toString() ?: System.getenv("DEFTU_PUBLISHING_USERNAME")
    }

    val publishingPassword: String? = run {
        return@run project.findProperty("deftu.publishing.password")?.toString() ?: System.getenv("DEFTU_PUBLISHING_PASSWORD")
    }

    repositories {
        mavenLocal()
        if (publishingUsername != null && publishingPassword != null) {
            fun MavenArtifactRepository.applyCredentials() {
                authentication.create<BasicAuthentication>("basic")
                credentials {
                    username = publishingUsername
                    password = publishingPassword
                }
            }

            maven {
                name = "DeftuReleases"
                url = uri("https://maven.deftu.xyz/releases")
                applyCredentials()
            }

            maven {
                name = "DeftuSnapshots"
                url = uri("https://maven.deftu.xyz/snapshots")
                applyCredentials()
            }
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = version
        }
    }
}
