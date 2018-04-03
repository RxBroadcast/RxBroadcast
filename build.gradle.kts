import com.jfrog.bintray.gradle.BintrayExtension
import info.solidsoft.gradle.pitest.PitestPluginExtension
import net.ltgt.gradle.errorprone.ErrorProneToolChain
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `maven-publish`
    java
    jacoco
    checkstyle
    findbugs
    pmd
    id("info.solidsoft.pitest") version "1.2.4"
    id("com.jfrog.bintray") version "1.6"
    id("net.ltgt.errorprone-base") version "0.0.13"
}

repositories {
    jcenter()
}

dependencies {
    compile("com.esotericsoftware:kryo:4.0.1")
    compile("com.google.protobuf:protobuf-java:3.5.0")
    compile("io.reactivex:rxjava:1.3.4")
    compile("org.jetbrains:annotations:15.0")
    errorprone("com.google.errorprone:error_prone_core:2.1.2")
    findbugsPlugins("com.mebigfatguy.fb-contrib:fb-contrib:7.0.5")
    testCompile("junit:junit:4.12")
    testCompile("nl.jqno.equalsverifier:equalsverifier:2.3.3")
}

fun linkGitHub(resource: String = "") = "https://github.com/${project.name}/${project.name}$resource"

val archivesBaseNameProperty = "archivesBaseName"
project.setProperty(archivesBaseNameProperty, project.name.toLowerCase())
val archivesBaseName = { "${project.property(archivesBaseNameProperty)}" }

group = project.name.toLowerCase()
version = "2.1.0"
description = "A small distributed event library for the JVM"

val testSourceSet = java.sourceSets["test"]!!
java.sourceSets.create("pitest") {
    java {
        srcDirs(testSourceSet.java.srcDirs)
        exclude("rxbroadcast/integration/**")
    }

    compileClasspath += files(testSourceSet.compileClasspath)
    runtimeClasspath += compileClasspath
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf("-Xlint:all", "-Xdiags:verbose", "-Werror"))
    sourceCompatibility = "${JavaVersion.VERSION_1_8}"
    targetCompatibility = "${JavaVersion.VERSION_1_8}"
}

tasks.withType<Javadoc> {
    options.optionFiles(file("config/javadoc.opts"))
}

tasks.withType<Test> {
    exclude("rxbroadcast/integration/**")
    testLogging({
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = true
    })
}

tasks.withType<FindBugs> {
    excludeFilter = file("${rootProject.projectDir}/config/findbugs/filters/exclude.xml")
    pluginClasspath = project.configurations["findbugsPlugins"]
    reports {
        xml.isEnabled = false
        html.isEnabled = true
    }
}

task("errorProne") {
    tasks.withType<JavaCompile>().all {
        toolChain = ErrorProneToolChain(configurations.getByName("errorprone"))
    }
    dependsOn.add(tasks.withType<JavaCompile>())
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

        from(sourceSets.findByName("main")!!.output + sourceSets.findByName("test")!!.output)
        from(files, {
            exclude("META-INF/**")
        })
    })
}

configure<PitestPluginExtension> {
    excludedMethods = setOf("toString", "newThread", "hashCode")
    detectInlinedCode = true
    timestampedReports = false
    mutationThreshold = 99
    mutators = setOf("DEFAULTS", "REMOVE_CONDITIONALS")
    testSourceSets = setOf(java.sourceSets["pitest"])
    verbose = System.getenv("CI").toBoolean()
}

task<Jar>("sourcesJar") {
    classifier = "sources"
    afterEvaluate({
        val sourceSets = convention.getPlugin(JavaPluginConvention::class).sourceSets
        from(sourceSets.findByName("main")!!.allSource)
    })
}

task<Jar>("javadocJar") {
    classifier = "javadoc"
    afterEvaluate({
        from(tasks.findByName("javadoc"))
    })
}

configure<CheckstyleExtension> {
    toolVersion = "8.2"
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
