plugins {
    `idea`
    `java`
    `maven-publish`
}

group = "io.spbx"
version = "0.1.0"

tasks.wrapper {
    gradleVersion = "8.10"
    jarFile = projectDir.resolve("gradle/wrapper/gradle-wrapper.jar")
    scriptFile = projectDir.resolve("gradle/wrapper/gradlew")
}

subprojects {
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
