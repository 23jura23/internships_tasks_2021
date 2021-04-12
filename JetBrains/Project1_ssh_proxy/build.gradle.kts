plugins {
    java
    application
}

group = "com.jetbrains.summer.ssh_proxy"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<Jar>("generalClientServerJar") {
    archiveBaseName.set("generalClientServer")

    manifest {
        attributes["Main-Class"] = "com.jetbrains.summer.ssh_proxy.general.GeneralClientServer"
    }

    from(sourceSets.main.get().output)
}


tasks.register<Jar>("clientJar") {
    archiveBaseName.set("client")

    manifest {
        attributes["Main-Class"] = "com.jetbrains.summer.ssh_proxy.Client"
    }

    from(sourceSets.main.get().output)
}

tasks.register<Jar>("serverJar") {
    archiveBaseName.set("server")

    manifest {
        attributes["Main-Class"] = "com.jetbrains.summer.ssh_proxy.Server"
    }

    from(sourceSets.main.get().output)
}

java {
    sourceSets {
        main {
            java.setSrcDirs(listOf("src/main"))
        }
    }
}

tasks.compileJava {
    options.release.set(11)
}

tasks.test {
    useJUnitPlatform()
}