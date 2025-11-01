plugins {
    id("java")
    application
    id("com.gradleup.shadow") version "8.3.1"
}

application.mainClass = "de.skyking_px.PhoenixBot.Bot"
group = "de.skyking_px"
version = "2.0.0-rc6"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:6.1.0")
    implementation("org.yaml:snakeyaml:2.5")
    implementation("ch.qos.logback:logback-classic:1.5.19")
    implementation ("org.apache.httpcomponents.client5:httpclient5:5.2")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    sourceCompatibility = "21"
}