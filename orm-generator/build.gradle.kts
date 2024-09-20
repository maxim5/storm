plugins {
    `java`
    `java-library`
    `java-test-fixtures`
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.28.0")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1.MR")

    implementation(project(":orm-api"))
    implementation("com.github.maxim5:java-basics:0.2.1")
    implementation("com.github.maxim5:prima:0.1.4")
    implementation("com.google.flogger:flogger:0.8")
    implementation("org.jetbrains:annotations:24.1.0")         // @NotNull and @Nullable are necessary at runtime
    implementation("com.google.guava:guava:33.2.0-jre")
}

dependencies {
    testFixturesImplementation("com.github.maxim5:java-basics:0.2.1")
    testFixturesImplementation("org.jetbrains:annotations:24.1.0")
    testFixturesImplementation("com.google.guava:guava:33.2.0-jre")
    testFixturesApi(testFixtures(project(":orm-api")))
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testCompileOnly("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("com.google.truth:truth:1.4.2")
    testRuntimeOnly("com.google.flogger:flogger-log4j2-backend:0.8")

    testImplementation("com.github.maxim5:java-basics:0.2.1:test-fixtures")
}
