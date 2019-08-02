#!/bin/sh

set -x
set -e

ROBOTS_SOURCE_REPO="${ROBOTS_SOURCE_REPO:-https://github.com/rh-jmc-team/jmc-robots-demo}"

COMMON_JAVA_ARGS="-Dcom.sun.management.jmxremote.rmi.port=9091 \
-Dcom.sun.management.jmxremote=true \
-Dcom.sun.management.jmxremote.port=9091 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.local.only=false \
-Dquarkus.http.host=0.0.0.0"

ROBOTS_BUILDER_IMAGE='fabric8/s2i-java:latest-java11'

create_robot_app() {
    project_name=$1
    app_name=$2
    rest_url=$3

    gradle_args=":${project_name}:build"
    if [ -n "${rest_url}" ]; then
        gradle_args="${gradle_args} -ProbotMakerURL=${rest_url}"
    fi
    copy_args='-r lib/ *-runner.jar'
    java_options="${COMMON_JAVA_ARGS} -Djava.rmi.server.hostname=${app_name}"

    echo "Creating application for subproject ${project_name}..."
    oc new-app --name="${app_name}" \
        --build-env="GRADLE_ARGS=${gradle_args}" \
        --build-env="ARTIFACT_DIR=${project_name}/build" \
        --build-env="ARTIFACT_COPY_ARGS=${copy_args}" \
        --env="JAVA_OPTIONS=${java_options}" \
        "${ROBOTS_BUILDER_IMAGE}~${ROBOTS_SOURCE_REPO}"
    # Patch the service to expose port 9091, which isn't exposed by the builder image
    oc patch "svc/${app_name}" -p '{"spec": {"ports": [{"name": "9091-tcp", "port": 9091, "protocol": "TCP", "targetPort": 9091}]}}'
}

if ! [ -x "$(command -v jq)" ]; then
    echo 'Error: jq is not installed.' >&2
    exit 1
fi

oc new-project robots

oc create sa discovery

oc create role service-lister --verb=list --resource=services

oc policy add-role-to-user --role-namespace=robots service-lister -z discovery

oc import-image --confirm "${ROBOTS_BUILDER_IMAGE}"

create_robot_app RobotMakerExpress2000 robotmaker

create_robot_app RobotShop robotshop 'http://robotmaker:8080'

create_robot_app RobotController robotcontroller 'http://robotmaker:8080'

oc new-app quay.io/rh-jmc-team/container-jfr-web:0.2.0 --name=container-jfr-web

oc delete svc container-jfr-web

oc expose dc container-jfr-web --target-port=8080 --port=80

oc expose svc container-jfr-web

oc new-app quay.io/rh-jmc-team/container-jfr:0.2.3 --name=container-jfr

oc patch dc/container-jfr -p '{"spec":{"template":{"spec":{"serviceAccountName":"discovery"}}}}'

oc set env dc/container-jfr CONTAINER_JFR_DOWNLOAD_PORT="8080"

oc expose dc container-jfr --name=container-jfr-exporter --port=8080

oc expose svc container-jfr-exporter

oc expose svc container-jfr --port=9090

CLIENT_URL="$(oc get route/container-jfr-exporter -o json | jq -r '.spec.host')"

WS_CLIENT_URL="ws://$(oc get route/container-jfr -o json | jq -r '.spec.host')/command"

oc set env dc/container-jfr CONTAINER_JFR_DOWNLOAD_HOST="$CLIENT_URL"

oc set env dc/container-jfr-web CONTAINER_JFR_URL="$WS_CLIENT_URL"

oc create -f "$(dirname "$(readlink -f "$0")")/persistent-volume-claim.yaml"

oc set volume dc/container-jfr \
    --add --claim-name "container-jfr" \
    --type="persistentVolumeClaim" \
    --mount-path="/flightrecordings" \
    --containers="*"

