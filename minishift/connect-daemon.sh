#!/bin/sh

nc $(minishift ip) $(oc get svc/jmx-client --output=json | jq '.spec.ports[0].nodePort')