#!/bin/sh

set -x
set -e

for i in container-jfr{,-web} ; do
    repository="quay.io/rh-jmc-team/${i}"
    oc import-image $repository:latest --from=$repository --confirm
done
