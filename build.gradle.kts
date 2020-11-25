//this works

import ProjectVersions.openosrsVersion

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    java //this enables annotationProcessor and implementation in dependencies
    checkstyle
}

project.extra["GithubUrl"] = "https://github.com/illumineawake/illu-plugins"

apply<BootstrapPlugin>()

allprojects {
    group = "com.openosrs.externals"
    apply<MavenPublishPlugin>()
}

allprojects {
    apply<MavenPublishPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

subprojects {
    group = "com.openosrs.externals"

    project.extra["PluginProvider"] = "illumine"
    project.extra["ProjectSupportUrl"] = "https://discord.gg/9fGzEDR"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    repositories {
        jcenter {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
            }
        }

        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            filter {
                includeGroupByRegex("com\\.openosrs.*")
                includeGroupByRegex("com\\.owain.*")
            }
        }
    }

    apply<JavaPlugin>()

    dependencies {
        annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.12")
        annotationProcessor(group = "org.pf4j", name = "pf4j", version = "3.4.1")
        implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
        implementation(group = "com.google.code.gson", name = "gson", version = "2.8.6")
        implementation(group = "com.google.guava", name = "guava", version = "29.0-jre")
        implementation(group = "com.google.inject", name = "guice", version = "4.2.3", classifier = "no_aop")
        implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.0")
        implementation(group = "io.reactivex.rxjava3", name = "rxjava", version = "3.0.6")
        implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")
        implementation(group = "org.apache.commons", name = "commons-text", version = "1.9")
        implementation(group = "org.pf4j", name = "pf4j", version = "3.4.1")
        implementation(group = "org.projectlombok", name = "lombok", version = "1.18.12")
        implementation(group = "org.pushing-pixels", name = "radiance-substance", version = "2.5.1")

        compileOnly("com.openosrs:runelite-api:$openosrsVersion+")
        compileOnly("com.openosrs.rs:runescape-api:$openosrsVersion+")
        compileOnly("com.openosrs:runelite-client:$openosrsVersion+")
        compileOnly("com.openosrs:http-api:$openosrsVersion+")

        compileOnly(Libraries.guice)
        compileOnly(Libraries.javax)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.pf4j)
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("$buildDir/repo")
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        register<Copy>("copyDeps") {
            into("./build/deps/")
            from(configurations["runtimeClasspath"])
        }

        withType<Jar> {
            doLast {
                copy {
                    from("./build/libs/")
                    into("C:/Users/Tolga/.runelite/externalmanager")
                }
            }
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }
    }
}