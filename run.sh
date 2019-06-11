#!/bin/sh

set -x

COMMON_JAVA_ARGS="-Dcom.sun.management.jmxremote.rmi.port=9091 \
-Dcom.sun.management.jmxremote=true \
-Dcom.sun.management.jmxremote.port=9091 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.local.only=false \
-Dquarkus.http.host=0.0.0.0"

docker_run() {
    container_name=$1

    java_options="${COMMON_JAVA_ARGS} -Djava.rmi.server.hostname=${container_name}"
    docker run \
        --net robot-demo \
        --name "${container_name}" \
        --memory 80M \
        -P \
        -e "JAVA_OPTIONS=${java_options}" \
        --rm -d "jmc-robots-demo/${container_name}"
}

function cleanup() {
    set +e
    # TODO: better container management
    for i in robotshop robotcontroller robotmaker; do
        docker kill "$(docker ps -a -q --filter ancestor=jmc-robots-demo/$i)"
        docker rm "$(docker ps -a -q --filter ancestor=jmc-robots-demo/$i)"
    done
}

cleanup
trap cleanup EXIT

set +e
docker network create --attachable robot-demo
set -e

for i in robotmaker robotcontroller robotshop; do
    docker_run "$i"
done

docker run \
    --net robot-demo \
    --name jmx-client \
    --memory 80M \
    -p 8080:8080 \
    --rm -it andrewazores/container-jmx-client "$@"
