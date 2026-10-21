#!/usr/bin/env bash
# Runs the hotels Firestore-rules acceptance test locally. No production Firebase
# credentials, database, storage bucket, or deployed rules are used.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
DEPS="${KARBAMA_TEST_DEPS:-/var/tmp/karbama-rules-test}"
mkdir -p "$DEPS"
if [[ ! -d "$DEPS/node_modules/@firebase/rules-unit-testing" ]]; then
  npm install --prefix "$DEPS" @firebase/rules-unit-testing firebase
fi
export NODE_PATH="$DEPS/node_modules${NODE_PATH:+:$NODE_PATH}"
# firebase-tools 13.x runs the current Firestore emulator on JDK 17. Keep this
# explicit so an old system Java cannot accidentally start the test.
if [[ -d /opt/kbtc/jdk17 ]]; then
  export JAVA_HOME=/opt/kbtc/jdk17
  export PATH="$JAVA_HOME/bin:$PATH"
fi
cd "$ROOT/Karbama"
npx --yes firebase-tools@13.35.1 emulators:exec --only firestore \
  --project karbama-hotels-demo \
  "node $ROOT/kb-tools/tests/firestore-hotels-rules.test.cjs"
