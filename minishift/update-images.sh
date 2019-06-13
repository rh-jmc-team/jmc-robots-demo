#!/bin/sh

for i in container-jmx-client container-jmc-web; do
    oc import-image andrewazores/$i:latest --from=docker.io/andrewazores/$i --confirm
done
