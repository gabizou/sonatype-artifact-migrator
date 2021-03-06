/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.7/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    // Use JCenter for resolving dependencies.
    jcenter()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.gabizou.Main")
    }
}

dependencies {
    implementation( group= "com.fasterxml.jackson.core", name= "jackson-core", version= "2.11.3")
    implementation( group= "com.fasterxml.jackson.core", name= "jackson-databind", version= "2.11.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation(group="org.apache.logging.log4j", name="log4j-core", version = "2.14.0")
}

application {
    // Define the main class for the application.
    mainClass.set("com.gabizou.Main")
    mainClassName = "com.gabizou.Main"
}
