#!/usr/bin/env bash
set -e

#docker build -t wms-loghub:native .


export JAVA_HOME=/Users/txm/.sdkman/candidates/java/21.0.9-graal
export PATH=$JAVA_HOME/bin:$PATH
export GRADLE_HOME=/Users/txm/.sdkman/candidates/gradle/9.2.0
export PATH=$GRADLE_HOME/bin:$PATH
# 加载环境变量
if [ -f ./.env ]; then
    set -a  # 自动导出所有变量
    . ./.env
    set +a
else
    echo "错误: .env 文件不存在"
    exit 1
fi

cat .env

# 检查必要变量
if [ -z "$MYSQL_URL" ]; then
    echo "错误: MYSQL_URL 环境变量未设置"
    exit 1
fi

echo "Run: ./build/native/nativeCompile/test"
./build/native/nativeCompile/test