#!/bin/bash
# نصب ابزار ساخت اندروید روی سندباکس تازه (فقط به اینترنت عمومی نیاز دارد)
set -x
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
if [ ! -d "$JAVA_HOME" ]; then
  sudo apt-get install -y -qq openjdk-21-jdk-headless unzip zip > /tmp/setup-apt.log 2>&1 || true
fi
if [ ! -f /swapfile ]; then sudo fallocate -l 4G /swapfile; sudo chmod 600 /swapfile; sudo mkswap /swapfile >/dev/null; fi
sudo swapon /swapfile 2>/dev/null || true
if [ ! -x /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager ]; then
  sudo mkdir -p /opt/android-sdk; sudo chown -R user:user /opt/android-sdk
  curl -sL -o /tmp/cmdline.zip https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
  rm -rf /tmp/cmdtools; mkdir -p /tmp/cmdtools; unzip -q -o /tmp/cmdline.zip -d /tmp/cmdtools
  mkdir -p /opt/android-sdk/cmdline-tools
  rm -rf /opt/android-sdk/cmdline-tools/latest
  mv /tmp/cmdtools/cmdline-tools /opt/android-sdk/cmdline-tools/latest
  yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses > /tmp/setup-lic.log 2>&1
fi
# Keep this outside the cmdline-tools bootstrap condition so an existing SDK is
# upgraded with the API 36 platform as well.
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses > /tmp/setup-lic.log 2>&1 || true
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0" > /tmp/setup-sdk.log 2>&1
sudo mkdir -p /opt/gradle-home; sudo chown -R user:user /opt/gradle-home
chmod +x /home/user/Karbama/gradlew
echo "=== SDK READY ==="
java -version 2>&1 | head -1
ls /opt/android-sdk
