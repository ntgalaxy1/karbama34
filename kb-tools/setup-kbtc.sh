#!/bin/bash
# Fresh-sandbox setup for the Karbama Android build (JDK17 + SDK 35 + swap + gh). Idempotent.
set -e
sudo mkdir -p /opt/kbtc && sudo chown user:user /opt/kbtc
if [ ! -x /opt/kbtc/jdk17/bin/java ]; then
  curl -sL -o /var/tmp/jdk17.tgz "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
  mkdir -p /opt/kbtc/jdk17 && tar xzf /var/tmp/jdk17.tgz -C /opt/kbtc/jdk17 --strip-components=1 && rm /var/tmp/jdk17.tgz
fi
if [ ! -d /opt/kbtc/android-sdk/build-tools/35.0.0 ]; then
  curl -sL -o /var/tmp/cmd.zip https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
  rm -rf /var/tmp/cmdt && unzip -q /var/tmp/cmd.zip -d /var/tmp/cmdt
  mkdir -p /opt/kbtc/android-sdk/cmdline-tools && rm -rf /opt/kbtc/android-sdk/cmdline-tools/latest
  mv /var/tmp/cmdt/cmdline-tools /opt/kbtc/android-sdk/cmdline-tools/latest && rm -rf /var/tmp/cmd.zip /var/tmp/cmdt
  S=/opt/kbtc/android-sdk/cmdline-tools/latest/bin/sdkmanager
  export JAVA_HOME=/opt/kbtc/jdk17
  yes | $S --sdk_root=/opt/kbtc/android-sdk --licenses >/dev/null 2>&1 || true
  $S --sdk_root=/opt/kbtc/android-sdk "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /var/tmp/sdk.log 2>&1
fi
if ! sudo swapon --show | grep -q swapfile; then
  [ -f /var/swapfile ] || { sudo fallocate -l 4G /var/swapfile; sudo chmod 600 /var/swapfile; sudo mkswap /var/swapfile >/dev/null; }
  sudo swapon /var/swapfile || true
fi
if [ ! -x /tmp/ghbin ]; then
  V=$(curl -s https://api.github.com/repos/cli/cli/releases/latest | grep -o '"tag_name": *"v[^"]*"' | grep -o 'v[0-9.]*')
  curl -sL -o /var/tmp/gh.tgz "https://github.com/cli/cli/releases/download/$V/gh_${V#v}_linux_amd64.tar.gz"
  tar xzf /var/tmp/gh.tgz -C /var/tmp && cp /var/tmp/gh_${V#v}_linux_amd64/bin/gh /tmp/ghbin && rm -rf /var/tmp/gh.tgz /var/tmp/gh_*
fi
cp /home/user/kb-tools/build.sh /tmp/build.sh; chmod +x /home/user/Karbama/gradlew
echo "=== READY ==="; /opt/kbtc/jdk17/bin/java -version 2>&1 | head -1; /tmp/ghbin --version | head -1
