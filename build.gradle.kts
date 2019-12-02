plugins {
  war
  base
  kotlin("jvm") version Globals.kotlinVersion
  kotlin("plugin.jpa") version Globals.kotlinVersion
  kotlin("plugin.noarg") version Globals.kotlinVersion
  kotlin("plugin.allopen") version Globals.kotlinVersion
  id("io.franzbecker.gradle-lombok") version Globals.lombokPluginVersion
  id("com.github.ben-manes.versions") version Globals.versionsPluginVersion
  id("fish.payara.micro-gradle-plugin") version Globals.payaraMicroPluginVersion
  // ./gradlew dependencyUpdates -Drevision=release
}

allprojects {
  group = Globals.groupId
  version = Globals.version
}

lombok {
  version = Globals.lombokVersion
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
  mavenCentral()
}

fun isAfterJdk8(): Boolean {
  val currentJavaVersion = org.gradle.internal.jvm.Jvm.current().javaVersion ?: JavaVersion.VERSION_1_8
  return currentJavaVersion.ordinal > JavaVersion.VERSION_1_8.ordinal
}

dependencies {
  // Liquibase
  implementation("org.liquibase:liquibase-core:${Globals.liquibaseVersion}")
  implementation("org.liquibase:liquibase-cdi:${Globals.liquibaseVersion}")

  // JPA
  implementation("com.h2database:h2:${Globals.h2Version}")
  providedCompile("javax.persistence:javax.persistence-api:${Globals.javaxPersistenceVersion}")

  if (isAfterJdk8()) { // JDK > 1.8
    implementation("javax.xml.bind:jaxb-api:${Globals.jaxbApiVersion}")
    implementation("org.glassfish.jaxb:jaxb-runtime:${Globals.jaxbRuntimeVersion}")
  }

  providedCompile("javax:javaee-api:${Globals.javaeeVersion}")
  implementation(platform("org.eclipse.microprofile:microprofile:${Globals.microprofileVersion}"))

  implementation("org.webjars:materializecss:${Globals.materializecssVersion}")
  implementation("org.webjars:material-design-icons:${Globals.materialDesignIconsVersion}")

  testImplementation("org.assertj:assertj-core:${Globals.assertjVersion}")
  testImplementation(platform("org.junit:junit-bom:${Globals.junitJupiterVersion}"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

  implementation(platform("org.apache.logging.log4j:log4j-bom:${Globals.log4jVersion}"))
  implementation("org.apache.logging.log4j:log4j-core")

  implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:${Globals.jacksonVersion}")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Globals.jacksonVersion}")
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
}

sourceSets {
  main {
    java.srcDirs(
            "src/main/java",
            "src/main/kotlin"
    )
  }
  test {
    java.srcDirs(
            "src/test/java",
            "src/test/kotlin"
    )
  }
}

allOpen {
  annotation("javax.ws.rs.Path")
  annotation("javax.ws.rs.ext.Provider")
  annotation("javax.persistence.Entity")
  annotation("javax.ws.rs.ApplicationPath")
  annotation("javax.enterprise.context.ApplicationScoped")
  annotation("javax.ejb.ConcurrencyManagement")
  annotation("javax.ejb.Singleton")
  annotation("javax.ejb.Startup")
}

noArg {
  annotation("javax.ws.rs.Path")
  annotation("javax.ws.rs.ext.Provider")
  annotation("javax.persistence.Entity")
  annotation("javax.ws.rs.ApplicationPath")
  annotation("javax.enterprise.context.ApplicationScoped")
  annotation("javax.ejb.ConcurrencyManagement")
  annotation("javax.ejb.Singleton")
  annotation("javax.ejb.Startup")
}

val defaultJavaOpts = mapOf(
  "Xdebug" to null,
  "Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" to null
)

val java9plusOpts = mapOf(
  "-illegal-access=permit" to null,
  "-add-modules=java.se" to null,
  "-add-exports=java.base/jdk.internal.ref=ALL-UNNAMED" to null,
  "-add-opens=java.base/java.lang=ALL-UNNAMED" to null,
  "-add-opens=java.base/java.nio=ALL-UNNAMED" to null,
  "-add-opens=java.base/sun.nio.ch=ALL-UNNAMED" to null,
  "-add-opens=java.management/sun.management=ALL-UNNAMED" to null,
  "-add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED" to null,
  "-add-opens=java.base/jdk.internal.loader=ALL-UNNAMED" to null,
  "-add-opens=jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED" to null
)

payaraMicro {
  daemon = false
  deployWar = false
  useUberJar = true
  payaraVersion = Globals.payaraMicroVersion
  commandLineOptions = mapOf("port" to 8080)
  javaCommandLineOptions =
      if (!isAfterJdk8()) defaultJavaOpts
      else defaultJavaOpts.plus(java9plusOpts)
}

tasks {
  val warTask = war.get()
  val cleanTask = clean.get()
  val assembleTask = assemble.get()
  val bundleTask = microBundle.get()
  val startTask = microStart.get()

  startTask.dependsOn(assembleTask.path)
  assembleTask.dependsOn(warTask.path, bundleTask.path)

  startTask.shouldRunAfter(cleanTask.path, assembleTask.path)
  assembleTask.shouldRunAfter(cleanTask.path, bundleTask.path)
  bundleTask.shouldRunAfter(cleanTask.path, warTask.path)
  warTask.shouldRunAfter(cleanTask.path)

  println("${org.gradle.internal.jvm.Jvm.current()} / ${org.gradle.util.GradleVersion.current()}")

  this.war {
    archiveFileName.set("ROOT.war")
  }

  named("clean") {
    delete(
        "$projectDir/out",
        buildDir
    )
  }

  withType<Wrapper> {
    gradleVersion = Globals.gradleWrapperVersion
    distributionType = Wrapper.DistributionType.BIN
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
      freeCompilerArgs.plus("-Xjsr305=strict")
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }

  withType<Test> {
    // useJUnitPlatform()
    testLogging {
      showExceptions = true
      showStandardStreams = true
      events(
              org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
              org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
              org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
      )
    }
  }
}

defaultTasks("clean", "build")
