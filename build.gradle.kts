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

val ENV = { key: String -> System.getenv(key) }

group = "io.github.null2264"
version = "1.0-SNAPSHOT"

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
    maven(url = "https://maven.aap.my.id/")
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("io.github.null2264:remap:1.0-SNAPSHOT")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
}

gradlePlugin {
    plugins {
        register("preprocess") {
            id = "io.github.null2264.preprocess"
            implementationClass = "com.replaymod.gradle.preprocess.PreprocessPlugin"
        }

        register("preprocess-root") {
            id = "io.github.null2264.preprocess-root"
            implementationClass = "com.replaymod.gradle.preprocess.RootPreprocessPlugin"
        }
    }
}


if (ENV("S3_ENDPOINT") != null) {
	System.setProperty("org.gradle.s3.endpoint", ENV("S3_ENDPOINT"))
}

publishing {
    val publishingPassword: String? = run {
        return@run System.getenv("MAVEN_PASS")
    }

    repositories {
        mavenLocal()
        if (ENV("AWS_ACCESS_KEY") != null && ENV("AWS_SECRET_KEY") != null) {
            maven {
                url = uri("s3://maven")
                credentials(AwsCredentials::class) {
                    accessKey = ENV("AWS_ACCESS_KEY")
                    secretKey = ENV("AWS_SECRET_KEY")
                }
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
