#!/usr/bin/env bash
set -e

#docker build -t wms-loghub:native .

export GRADLE_USER_HOME=$HOME/.gradle9
export JAVA_HOME=/Users/txm/.sdkman/candidates/java/21.0.9-graal
export PATH=$JAVA_HOME/bin:$PATH
export GRADLE_HOME=/Users/txm/.sdkman/candidates/gradle/9.2.0
export PATH=$GRADLE_HOME/bin:$PATH

echo "Using Java from: $JAVA_HOME"
echo "Using Gradle from: $GRADLE_HOME"

echo "Building with GraalVM..."
gradle -v
#gradle clean --info
gradle nativeCompile --no-daemon --info

echo "Native image built. To run the native application:"
echo "Find the executable in build/native/nativeCompile/ directory"
echo "Run: ./build/native/nativeCompile/test"