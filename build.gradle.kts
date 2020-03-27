import com.mkobit.jenkins.pipelines.http.AnonymousAuthentication

plugins {
    java
    groovy
    jacoco
    id("com.mkobit.jenkins.pipelines.shared-library") version "0.10.1"
    id("com.github.ben-manes.versions") version "0.28.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val junitVersion = "5.6.1"
val spockVersion = "1.3-groovy-2.4"
val groovyVersion = "2.4.19"
val slf4jVersion = "1.8.0-beta4"
var jacksonVersion = "2.9.8"

dependencies {
    implementation("org.codehaus.groovy", "groovy-all", groovyVersion)

    // jackson
    implementation("com.fasterxml.jackson.module", "jackson-module-jsonSchema", jacksonVersion)

    // unit-tests
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)

    testImplementation("org.assertj", "assertj-core", "3.15.0")
    testImplementation("org.mockito", "mockito-core", "3.3.3")

    testImplementation("org.slf4j", "slf4j-api", slf4jVersion)
    testImplementation("org.slf4j", "slf4j-simple", slf4jVersion)

    // integration-tests
    integrationTestImplementation("org.spockframework", "spock-core", spockVersion)
    integrationTestImplementation("org.codehaus.groovy", "groovy-all", groovyVersion)

    integrationTestImplementation("org.slf4j", "slf4j-api", slf4jVersion)
    integrationTestImplementation("org.slf4j", "slf4j-simple", slf4jVersion)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }

    reports {
        html.isEnabled = true
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.integrationTest)
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        xml.destination = File("$buildDir/reports/jacoco/test/jacoco.xml")
    }
}

jenkinsIntegration {
    baseUrl.set(uri("http://localhost:5050").toURL())
    authentication.set(providers.provider { AnonymousAuthentication })
    downloadDirectory.set(layout.projectDirectory.dir("jenkinsResources"))
}

sharedLibrary {
    // TODO: this will need to be altered when auto-mapping functionality is complete
    coreVersion.set(jenkinsIntegration.downloadDirectory.file("core-version.txt").map { it.asFile.readText().trim() })
    // TODO: retrieve downloaded plugin resource
    pluginDependencies {
        dependency("org.jenkins-ci.plugins", "pipeline-build-step", "2.12")
        dependency("org.6wind.jenkins", "lockable-resources", "2.7")
        val declarativePluginsVersion = "1.6.0"
        dependency("org.jenkinsci.plugins", "pipeline-model-api", declarativePluginsVersion)
        dependency("org.jenkinsci.plugins", "pipeline-model-declarative-agent", "1.1.1")
        dependency("org.jenkinsci.plugins", "pipeline-model-definition", declarativePluginsVersion)
        dependency("org.jenkinsci.plugins", "pipeline-model-extensions", declarativePluginsVersion)
    }
}
