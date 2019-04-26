#!/bin/sh

oc delete --ignore-not-found services,pods,routes -l name=jmxclient