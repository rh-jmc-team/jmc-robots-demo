#!/bin/bash

set -e
set -x

for i in RobotController RobotMakerExpress2000 RobotShop; do
    echo "Building subproject $i..."
    pushd $i
    ./gradlew jibDockerBuild
    popd
done