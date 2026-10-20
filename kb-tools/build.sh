#!/bin/bash
# ساخت اندروید بدون پر کردن فضای کاری:
# کش Gradle و خروجی‌های موقت بیرون از /home/user می‌مانند.
export JAVA_HOME=/opt/kbtc/jdk17 PATH=/opt/kbtc/jdk17/bin:$PATH
export ANDROID_HOME=/opt/kbtc/android-sdk ANDROID_SDK_ROOT=/opt/kbtc/android-sdk
export GRADLE_USER_HOME=/var/tmp/gradle-home
cd /home/user/Karbama
bash ./gradlew --no-daemon --project-cache-dir /var/tmp/gradle-project-cache \
  -Pkotlin.compiler.execution.strategy=in-process "$@"
