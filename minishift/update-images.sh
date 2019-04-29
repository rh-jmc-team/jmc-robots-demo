#!/bin/sh

for i in robotmaker robotcontroller robotshop container-jmx-client; do
    oc import-image andrewazores/$i:latest --from=docker.io/andrewazores/$i --confirm
done
