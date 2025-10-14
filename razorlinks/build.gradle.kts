plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
    id("gg.jte.gradle") version "3.2.1"
}

group = "com.razorquake"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("com.mysql:mysql-connector-j")
	runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    testImplementation("org.springframework.security:spring-security-test")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("com.warrenstrange:googleauth:1.4.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    implementation("gg.jte:jte:3.2.1")
    implementation("gg.jte:jte-spring-boot-starter-3:3.2.1")
}

// JTE Configuration
jte {
    // Generate templates to build/jte-classes
    generate()

    // Source directory for templates
    sourceDirectory = file("src/main/jte").toPath()

    // Where to generate compiled templates
    targetDirectory = file("build/jte-classes").toPath()

    // Content type
    contentType = gg.jte.ContentType.Html

}

// Make sure JTE templates are compiled before building
tasks.named("compileJava") {
    dependsOn("precompileJte")
}

// Include compiled templates in the JAR
tasks.named<Jar>("jar") {
    from("build/jte-classes") {
        into("jte-classes")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    from("build/jte-classes") {
        into("BOOT-INF/classes/jte-classes")
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
