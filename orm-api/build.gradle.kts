plugins {
    `java`
    `java-library`
    `java-test-fixtures`
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.google.errorprone:error_prone_annotations:2.28.0")

    compileOnly("com.google.flogger:flogger:0.8")

    implementation("com.github.maxim5:java-basics:0.1.2")
    implementation("com.github.maxim5:prima:0.1.1")

    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("com.carrotsearch:hppc:0.10.0")
}

dependencies {
    testFixturesImplementation("com.google.truth:truth:1.4.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")

    testFixturesImplementation("org.jetbrains:annotations:24.1.0")
    testFixturesImplementation("com.carrotsearch:hppc:0.10.0")
    testFixturesImplementation("com.mockrunner:mockrunner-jdbc:2.0.7")
    testFixturesImplementation("com.github.maxim5:java-basics:0.1.2:test-fixtures")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testCompileOnly("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

    testCompileOnly("org.jetbrains:annotations:24.1.0")
    testImplementation("com.google.truth:truth:1.4.2")

    testImplementation("com.mockrunner:mockrunner-jdbc:2.0.7")
    testImplementation("com.github.maxim5:java-basics:0.1.2:test-fixtures")
    testImplementation("com.github.maxim5:prima:0.1.1:test-fixtures")
}

tasks.test {
    useJUnitPlatform()
}
