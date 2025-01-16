plugins {
    `idea`
    `java`
    `java-library`
    `maven-publish`
}

group = "io.spbx"
version = "0.3.1"

tasks.wrapper {
    gradleVersion = "8.10"
    jarFile = projectDir.resolve("gradle/wrapper/gradle-wrapper.jar")
    scriptFile = projectDir.resolve("gradle/wrapper/gradlew")
}

allprojects {
    apply(plugin = "idea")
    apply(plugin = "java")

    idea {
        module {
            outputDir = buildDir.resolve("idea/main")
            testOutputDir = buildDir.resolve("idea/test")
            isDownloadJavadoc = false
            isDownloadSources = true
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

dependencies {
    api(project(":orm-api"))
    api(project(":orm-generator"))
}

subprojects {
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString()
                artifactId = project.name.replace("orm-", "storm-")
                version = rootProject.version.toString()
                from(components["java"])
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "storm"
            version = project.version.toString()
            from(components["java"])
        }
    }
}
