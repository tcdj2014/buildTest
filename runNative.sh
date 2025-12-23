#!/usr/bin/env bash
set -e

#docker build -t wms-loghub:native .


export JAVA_HOME=/Users/txm/.sdkman/candidates/java/21.0.9-graal
export PATH=$JAVA_HOME/bin:$PATH
export GRADLE_HOME=/Users/txm/.sdkman/candidates/gradle/9.2.0
export PATH=$GRADLE_HOME/bin:$PATH

echo "Run: ./build/native/nativeCompile/test"
./build/native/nativeCompile/test