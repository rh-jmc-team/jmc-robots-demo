#!/bin/bash

set -e
set -x

s2i_build() {
    project_name=$1
    image_tag=$2
    rest_url=$3

    builder_image='fabric8/s2i-java:latest-java11'
    gradle_args=":${project_name}:build"
    if [ -n "${rest_url}" ]; then
        gradle_args="${gradle_args} -ProbotMakerURL=${rest_url}"
    fi
    copy_args='-r lib/ *-runner.jar'

    echo "Building subproject ${project_name}..."
    s2i build --copy -e GRADLE_ARGS="${gradle_args}" -e ARTIFACT_DIR="${project_name}/build" \
        -e ARTIFACT_COPY_ARGS="${copy_args}" . "${builder_image}" "${image_tag}"
}

# Check s2i is available on PATH
command -v s2i >/dev/null || { echo -e 'Please ensure s2i is installed and on your PATH
See: https://github.com/openshift/source-to-image' >&2; exit 1; }

s2i_build RobotMakerExpress2000 jmc-robots-demo/robotmaker
s2i_build RobotShop jmc-robots-demo/robotshop 'http://robotmaker:8080'
s2i_build RobotController jmc-robots-demo/robotcontroller 'http://robotmaker:8080'
