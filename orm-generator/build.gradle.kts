plugins {
    `java`
    `java-library`
    `java-test-fixtures`
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.28.0")
    compileOnly("com.google.flogger:flogger:0.8")
    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1.MR")

    implementation("org.jetbrains:annotations:24.1.0")         // @NotNull and @Nullable are necessary at runtime
    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("com.google.mug:mug:7.2")

    api(project(":orm-api"))
    api("com.github.maxim5:java-basics:0.1.2")
    api("com.github.maxim5:prima:0.1.1")
}

dependencies {
    testFixturesImplementation("org.jetbrains:annotations:24.1.0")
    testFixturesImplementation("com.google.guava:guava:33.2.0-jre")
    testFixturesApi(testFixtures(project(":orm-api")))
}

dependencies {
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")

    testImplementation("com.github.maxim5:java-basics:0.1.2:test-fixtures")
}
