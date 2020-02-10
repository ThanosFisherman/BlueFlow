import org.jetbrains.kotlin.konan.properties.hasProperty
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    // Documentation for our code
    id("org.jetbrains.dokka") version Versions.DOKKA
    // Publication to bintray
    id("com.jfrog.bintray") version Versions.BINTRAY
    // Maven publication
    `maven-publish`
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")


    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(29)
        versionCode = Artifact.versionCode
        versionName = Artifact.artifactVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.KOTLIN}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Coroutines}")
    implementation("androidx.core:core-ktx:${Versions.KTX}")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}


val dokkaTask by tasks.creating(org.jetbrains.dokka.gradle.DokkaTask::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/dokka"
}

val dokkaJar by tasks.creating(Jar::class) {
    archiveClassifier.set("dokka")
    from("$buildDir/dokka")
    dependsOn(dokkaTask)
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

publishing {
    publications {
        create<MavenPublication>(Artifact.artifactName) {
            groupId = Artifact.artifactGroup
            artifactId = Artifact.artifactName
            version = Artifact.artifactVersion
            //from(components["java"])
            artifacts {
                artifact("$buildDir/outputs/aar/${project.name}-release.aar")
                artifact(sourcesJar)
                artifact(dokkaJar)
            }

            pom.withXml {
                asNode().apply {
                    appendNode("description", Artifact.pomDesc)
                    appendNode("name", Artifact.libName)
                    appendNode("url", Artifact.pomUrl)
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", Artifact.pomLicenseName)
                        appendNode("url", Artifact.pomLicenseUrl)
                        appendNode("distribution", Artifact.pomLicenseDist)
                    }
                    appendNode("developers").appendNode("developer").apply {
                        appendNode("id", Artifact.pomDeveloperId)
                        appendNode("name", Artifact.pomDeveloperName)
                    }
                    appendNode("scm").apply {
                        appendNode("url", Artifact.pomScmUrl)
                    }
                }
            }
        }
    }
}

bintray {
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())

    // Getting bintray user and key from properties file or command line
    user =
        if (properties.hasProperty("bintrayUser")) properties.getProperty("bintrayUser") as String else "thanosfisherman"
    key =
        if (properties.hasProperty("bintrayKey")) properties.getProperty("bintrayKey") as String else ""

    // Automatic publication enabled
    publish = true
    dryRun = false

    // Set maven publication onto bintray plugin
    setPublications(Artifact.artifactName)

    // Configure package
    pkg.apply {
        repo = "maven"
        name = Artifact.bintrayName
        setLicenses("Apache-2.0")
        setLabels("Kotlin", "android", "bluetooth", "coroutines", "flow")
        vcsUrl = Artifact.pomScmUrl
        websiteUrl = Artifact.pomUrl
        issueTrackerUrl = Artifact.pomIssueUrl
        githubRepo = Artifact.githubRepo
        githubReleaseNotesFile = Artifact.githubReadme

        // Configure version
        version.apply {
            name = Artifact.artifactVersion
            desc = Artifact.pomDesc
            released = Date().toString()
            vcsTag = Artifact.artifactVersion
        }
    }
}