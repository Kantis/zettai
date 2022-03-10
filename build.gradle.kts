import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Note: AWS supports only JDK 11 for Lambdas so far
val targetJdk = JavaVersion.VERSION_17

plugins {
    kotlin("jvm") version "1.6.10"
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "7.4"
    distributionType = Wrapper.DistributionType.ALL
}

dependencies {
    testImplementation(Testing.kotest.runner.junit5)
    testImplementation("io.kotest.extensions:kotest-property-arbs:_")
}

allprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = targetJdk.toString()
        }
    }

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = targetJdk
            targetCompatibility = targetJdk
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
