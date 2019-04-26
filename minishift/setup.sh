#!/bin/sh

oc new-project robots

for i in robotmaker robotcontroller robotshop; do
    oc new-app docker.io/andrewazores/$i
done
