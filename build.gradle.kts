plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.jfrog.bintray")
}

val projectGroup: String by project
val projectVersion: String by project
val projectDescription: String by project
group = projectGroup
version = projectVersion
description = projectDescription

repositories {
    jcenter()
}

val jUnitVersion: String by project
val gRPCVersion: String by project
val mockitoVersion: String by project
val jUnitTestkitVersion: String by project
val coroutinesVersion: String by project
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug")
    implementation(platform("org.junit:junit-bom:$jUnitVersion"))
    implementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-testkit:$jUnitTestkitVersion")
}

ext {
    // This should be same as you've created in bintray
    bintrayRepo = "wonderful"

    // Name which will be visible on bintray
    bintrayName = "CheckLibrary"

    // Repository Link (For e.g. GitHub repo)
    siteUrl = "https://github.com/testacc-art/coroutines-test"
    gitUrl = "https://github.com/testacc-art/coroutines-test.git"
    githubRepository= "testacc-art/coroutines-test"

    // License Details
    licenseName = "The Apache Software License, Version 2.0"
    licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    allLicenses = ["Apache-2.0"]
}

plugins.withType<JavaPlugin> {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        jvmTarget = "1.8"
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

tasks.test {
    testLogging {
        showStandardStreams = true
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates a sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val kdocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates KDoc"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

tasks.jar.configure {
    finalizedBy(sourcesJar, kdocJar)
}

val licenseName: String by project
val licenseUrl: String by project
val developerName: String by project
val developerEmail: String by project
val gitHubUsername: String by project

val gitHubUrl: String by lazy { "github.com/$gitHubUsername/${project.name}" }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(kdocJar)
            artifact(sourcesJar)
            pom {
                name.set("${project.group}:${project.name}")
                description.set(project.description)
                url.set("https://$gitHubUrl")
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
                scm {
                    connection.set("scm:git:git://$gitHubUrl.git")
                    developerConnection.set("scm:git:ssh://github.com:$gitHubUsername/${project.name}.git")
                    url.set("https://$gitHubUrl")
                }
            }
        }
    }
}

bintray {
    user = System.getenv("bintrayUser")
    key = System.getenv("bintrayApiKey")

    configurations = ['archives']
    pkg {
        repo = 
        name = bintrayName
        websiteUrl = siteUrl
        vcsUrl = gitUrl
        licenses = allLicenses
        publish = true
    }
}
