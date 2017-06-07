import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `maven-publish`
    java
    jacoco
    checkstyle
    id("com.jfrog.bintray") version "1.6"
}

repositories {
    jcenter()
}

dependencies {
    compile("com.esotericsoftware:kryo:3.0.3")
    compile("com.google.protobuf:protobuf-java:3.3.0")
    compile("io.reactivex:rxjava:1.3.0")
    compile("org.jetbrains:annotations:15.0")
    testCompile("junit:junit:4.12")
}

fun linkGitHub(resource: String = "") = "https://github.com/${project.name}/${project.name}$resource"

val archivesBaseNameProperty = "archivesBaseName"
project.setProperty(archivesBaseNameProperty, project.name.toLowerCase())
val archivesBaseName = { "${project.property(archivesBaseNameProperty)}" }

group = project.name.toLowerCase()
version = "1.2.0"
description = "A small distributed event library for the JVM"

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf("-Xlint:all", "-Xdiags:verbose", "-Werror"))
    sourceCompatibility = "${JavaVersion.VERSION_1_8}"
    targetCompatibility = "${JavaVersion.VERSION_1_8}"
}

tasks.withType<Javadoc> {
    options.optionFiles(file("config/javadoc.opts"))
}

tasks.withType<Test> {
    exclude("rx/broadcast/integration/**")
    testLogging({
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = true
    })
}

task<Jar>("testJar") {
    classifier = "tests"
    group = "verification"
    description = "Assembles a jar archive containing the test classes."
    afterEvaluate({
        val sourceSets = convention.getPlugin(JavaPluginConvention::class).sourceSets
        val files = configurations.testCompile.files.map(fun (file: File): Any = when {
            file.isDirectory -> file
            else -> zipTree(file)
        })

        from(sourceSets.findByName("main").output + sourceSets.findByName("test").output)
        from(files, {
            exclude("META-INF/**")
        })
    })
}

task<Jar>("sourcesJar") {
    classifier = "sources"
    afterEvaluate({
        val sourceSets = convention.getPlugin(JavaPluginConvention::class).sourceSets
        from(sourceSets.findByName("main").allSource)
    })
}

task<Jar>("javadocJar") {
    classifier = "javadoc"
    afterEvaluate({
        from(tasks.findByName("javadoc"))
    })
}

configure<CheckstyleExtension> {
    toolVersion = "6.15"
}

configure<JacocoPluginExtension> {
    toolVersion = "0.7.9"
}

configure<PublishingExtension> {
    publications {
        create(project.name.toLowerCase(), MavenPublication::class.java, {
            from(components.findByName("java"))
            artifactId = archivesBaseName()
            artifact(tasks.getByName("javadocJar"))
            artifact(tasks.getByName("sourcesJar"))
            pom {
                withXml {
                    asNode().apply {
                        appendNode("name", project.name)
                        appendNode("description", project.description)
                        appendNode("url", "http://${project.name.toLowerCase()}.website")
                        appendNode("packaging", "jar")

                        appendNode("licenses").appendNode("license").apply {
                            appendNode("name", "ISC")
                            appendNode("url", linkGitHub("/raw/master/LICENSE.md"))
                        }

                        appendNode("scm").apply {
                            appendNode("url", linkGitHub())
                        }

                        appendNode("issueManagement").apply {
                            appendNode("system", "GitHub Issues")
                            appendNode("url", linkGitHub("/issues"))
                        }
                    }
                }
            }
        })
    }
}

configure<BintrayExtension> {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")

    setPublications(project.name.toLowerCase())
    publish = true

    pkg = PackageConfig().apply {
        repo = "maven"
        name = project.name
        vcsUrl = linkGitHub()
        setLicenses("ISC")
        version = VersionConfig().apply {
            name = project.version.toString()
            desc = project.description
        }
    }
}
