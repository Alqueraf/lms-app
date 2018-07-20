import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "cloud_build_api"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", Versions.KOTLIN))
    }
}

plugins {
    application
}

apply {
    plugin("kotlin")
}

application {
    mainClassName = "main"
}

repositories {
    jcenter()
}

dependencies {
    // retrofit / okhttp
    compile("com.squareup.retrofit2:retrofit:2.2.0")
    compile("com.squareup.retrofit2:converter-gson:2.2.0")
    compile("com.squareup.okhttp3:logging-interceptor:3.4.1")
    compile("com.squareup.okhttp3:okhttp:3.7.0")
    compile("com.google.code.gson:gson:2.8.0")
    compile(kotlin("stdlib-jre8", Versions.KOTLIN))

    // https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.google.cloud%22%20a%3A%22google-cloud-datastore%22
    compile("com.google.cloud:google-cloud-datastore:1.8.0")

    // https://cloud.google.com/storage/docs/reference/libraries#client-libraries-install-java
    compile("com.google.cloud:google-cloud-storage:1.16.0")

    // Google Sheets https://developers.google.com/sheets/api/quickstart/java
    compile("com.google.apis:google-api-services-sheets:v4-rev489-1.23.0")
    compile("com.google.api-client:google-api-client:1.23.0")
    compile("com.google.oauth-client:google-oauth-client-jetty:1.23.0")

    // yaml parsing
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.5")

    // mockito inline enables mocking of final classes by default
    testCompile("org.mockito:mockito-inline:2.11.0")
    testCompile("junit:junit:4.12")
    testCompile("org.hamcrest:hamcrest-junit:2.0.0.0")
    testCompile("org.hamcrest:java-hamcrest:2.0.0.0")
}
// Fix Exception in thread "main" java.lang.NoSuchMethodError: com.google.common.util.concurrent.MoreExecutors.directExecutor()Ljava/util/concurrent/Executor;
// Ensure all deps are using the same modern version of guava
configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:23.6-jre")
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
}

val javaVersion = "1.8"
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = javaVersion
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = javaVersion
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Output full test results to console
// Avoids having to read the HTML report
tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// gradle fatJar
// java -jar build/libs/cloud_build_metrics-all-1.0-SNAPSHOT.jar
task("fatJar", type = Jar::class) {
    baseName = "${project.name}-all"
    manifest {
        attributes.apply {
            put("Main-Class", "tasks.Main")
        }
    }
    from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
}
