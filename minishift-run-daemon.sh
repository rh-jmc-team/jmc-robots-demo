#!/bin/sh

set -x
set -e

LABEL="name=jmxclient"

oc run \
    --labels="$LABEL" \
    --restart=Never \
    --image=docker.io/andrewazores/container-jmx-client:latest --image-pull-policy=Always \
    -- jmx-client -d

oc expose pod jmx-client -l "$LABEL" --name=jmx-client-exporter --port=8080
oc expose svc jmx-client-exporter -l "$LABEL" --name=jmx-client-exporter --port=8080 --generator=route/v1

oc expose pod jmx-client -l "$LABEL" --port=9090 --generator=service/v2 --type=NodePort

echo "Connect to the container-jmc client instance with 'nc $(minishift ip) $(oc get svc/jmx-client --output=json | jq '.spec.ports[0].nodePort')'"
