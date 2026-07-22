plugins {
    id("java-library")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.nexomc.com/releases")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("net.kyori:adventure-text-minimessage:5.2.0")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("beer.devs:itemsadder-api:4.0.18-beta-10")
    compileOnly("net.momirealms:craft-engine-core:26.7")
    compileOnly("net.momirealms:craft-engine-bukkit:26.7")
    compileOnly("com.nexomc:nexo:1.25.0")
    compileOnly("io.th0rgal:oraxen:1.217.0")

    implementation("org.bstats:bstats-bukkit:3.2.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("")

    configurations = project.configurations.runtimeClasspath.map { setOf(it) }

    dependencies {
        // Only merge bStats into the final jar, no other dependencies
        exclude { it.moduleGroup != "org.bstats" }
    }

    // Relocate bStats into the plugin's package to avoid conflicts with other
    // plugins using bStats
    relocate("org.bstats", "minej.minejango2.chatitemsprite.libs.bstats")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}
