#!/bin/bash
set -euxo pipefail

./gradlew clean fatJar

cp ./build/libs/cluster_create-all-*.jar ./cluster_create.jar
