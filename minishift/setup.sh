#!/bin/sh

set -x
set -e

oc new-project robots

for i in robotmaker robotcontroller robotshop container-jmc-web; do
    oc new-app docker.io/andrewazores/$i --name=$i
done

oc expose svc container-jmc-web --port=8080

oc new-app docker.io/andrewazores/container-jmx-client --name=jmx-client

oc set env dc/jmx-client CONTAINER_DOWNLOAD_PORT="8080"

oc expose dc jmx-client --name=jmx-client-exporter --port=8080

oc expose svc jmx-client-exporter

oc expose svc jmx-client --port=9090

oc set env dc/jmx-client CONTAINER_DOWNLOAD_HOST="$(oc get route/jmx-client-exporter -o json | jq -r '.spec.host')"
