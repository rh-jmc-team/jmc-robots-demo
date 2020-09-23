#!/bin/sh

HOST="$(oc get route/jmx-client-exporter | sed -n 2p | tr -s ' ' | cut -d ' ' -f 2)"

if [ -z $HOST ]; then
    exit 1
fi

wget "http://$HOST/$1" -O "$1.jfr"
