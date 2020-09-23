#!/bin/sh

set -x
set -e

LABEL="name=jmxclient"

function cleanup() {
    oc delete --ignore-not-found pods,services,routes -l "$LABEL" > /dev/null 2>&1
}

function oc_expose() {
    sleep 10 # TODO replace this with a proper wait for the pod to be available
    oc expose -l "$LABEL" svc/jmx-client --name=jmx-client-exporter
}

trap cleanup EXIT
cleanup
oc_expose > /dev/null 2>&1 &

oc run \
    --labels="$LABEL" \
    --restart=Never \
    --rm \
    --wait \
    --attach --stdin --tty \
    --port=8080 --expose \
    --image=docker.io/andrewazores/container-jmx-client:latest --image-pull-policy=Always \
    -- jmx-client "$@"
