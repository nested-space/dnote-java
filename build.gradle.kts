plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.4"
    id("io.micronaut.aot") version "4.4.4"
}

version = "0.4"
group = "uk.co.nestedspace"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.views:micronaut-views-pebble")
    implementation("io.micronaut:micronaut-http-client")

    implementation("io.micronaut.rxjava3:micronaut-rxjava3")
    implementation("io.reactivex.rxjava3:rxjava:3.1.7")

    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("io.micronaut:micronaut-http-client")
}


application {
    mainClass = "uk.co.nestedspace.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}


graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("uk.co.nestedspace.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}


