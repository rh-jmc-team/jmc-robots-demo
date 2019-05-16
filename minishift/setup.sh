#!/bin/sh

set -x
set -e

oc new-project robots

for i in robotmaker robotcontroller robotshop container-jmc-web; do
    oc new-app docker.io/andrewazores/$i --name=$i
done

oc delete svc container-jmc-web

oc expose dc container-jmc-web --target-port=8080 --port=80

oc expose svc container-jmc-web

oc new-app docker.io/andrewazores/container-jmx-client --name=jmx-client

oc set env dc/jmx-client CONTAINER_DOWNLOAD_PORT="8080"

oc expose dc jmx-client --name=jmx-client-exporter --port=8080

oc expose svc jmx-client-exporter

oc expose svc jmx-client --port=9090

CLIENT_URL="$(oc get route/jmx-client-exporter -o json | jq -r '.spec.host')"

WS_CLIENT_URL="ws://$(oc get route/jmx-client -o json | jq -r '.spec.host')/command"

oc set env dc/jmx-client CONTAINER_DOWNLOAD_HOST="$CLIENT_URL"

oc set env dc/container-jmc-web CONTAINER_JMX_CLIENT_URL="$WS_CLIENT_URL"
