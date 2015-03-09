# Crash-Proof support for Android apps

There is two approaches to add crash-proof support to your apps. The basic approach is to call `Thread.setDefaultUncaughtExceptionHandler()`. The drawback is that the app cannot resume its execution from the point of crash. So this approach is pretty meaningless because there is no way your app can continue normally.

This project shows you the advanced approach. By integrating AspectJ to your app, it is very easy to catch any unchecked exceptions that may crash your app unexpectedly. Moreover, this approach is very simple and easily maintainable.

1. Gradle setup
---------------
Below is a sample `build.gradle` of your app module.

<pre>
import com.android.build.gradle.AppPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.+'
        // Adds AspectJ support
        classpath 'org.aspectj:aspectjtools:1.+'
    }
}

apply plugin: 'com.android.application'

repositories {
    mavenCentral()
}

android {
    compileSdkVersion 21
    buildToolsVersion "21.1.2"

    defaultConfig {
        applicationId "android.app.crashproof"
        minSdkVersion 9
        targetSdkVersion 21
        versionCode 1
        versionName "1.0"
        // You may need to enable multi-dex support because AspectJ will increase the method count by ~60%
        multiDexEnabled true
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

    lintOptions {
        abortOnError false
    }

    // Enables multi-dex support because AspectJ will increase the method count by ~60%
    dexOptions {
        jumboMode = true
        preDexLibraries = false
        javaMaxHeapSize "1536m" // Modify this number as you like. 1.5g should be enough for a large project.
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    // Adds AspectJ support
    compile 'org.aspectj:aspectjrt:1.+'
}

android.applicationVariants.all { variant ->
    AppPlugin plugin = project.plugins.getPlugin(AppPlugin)
    JavaCompile javaCompile = variant.javaCompile

    // Weaves code at the last step of compilation
    javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.5",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", plugin.project.android.bootClasspath.join(File.pathSeparator)]

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler)

        def log = project.logger

        // Outputs weaver logs to the default logger
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }
}
</pre>

 1. Create `Aspect` for method interception
------------------------------------------
This project contains an aspect, `UncheckedExceptionAspect`, which you can customize to suit your needs. It does 2 things:
 1. Intercepts `Application.onCreate()` so that `Thread.setDefaultUncaughtExceptionHandler()` is called before `Application.onCreate()`.
 2. Wraps all methods and constructors with a try-catch block so that if it throws any unchecked exception, we will swallow it and log it to the console.

**You must at least customize the pointcut definitions in the sample aspect class so that it works for your package name.**

3. Extends `Application`
------------------------
You should extend `Application` class so that our aspect defined above can call `Thread.setDefaultUncaughtExceptionHandler()` before `Application.onCreate()` is called.
