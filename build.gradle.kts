buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {

        classpath("com.android.tools.build:gradle:3.6.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
    }
}
allprojects {

    repositories {
        jcenter()
        google()
    }
}

/*
tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}*/
