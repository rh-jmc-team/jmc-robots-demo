#!/bin/sh

set -x
set -e

oc new-project robots

for i in robotmaker robotcontroller robotshop container-jmc-web; do
    oc new-app docker.io/andrewazores/$i --name=$i
done

oc expose svc container-jmc-web --port=8080

oc new-app docker.io/andrewazores/container-jmx-client --name=jmx-client

oc expose svc jmx-client --port=9090
