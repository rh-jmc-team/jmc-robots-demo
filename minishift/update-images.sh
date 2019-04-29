#!/bin/sh

for i in robotmaker robotcontroller robotshop container-jmx-client; do
    oc import-image andrewazores/$i:latest --from=andrewazores/$i --confirm
done
