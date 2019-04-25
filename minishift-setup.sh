#!/bin/sh

for i in robotmaker robotcontroller robotshop; do
    oc new-app docker.io/andrewazores/$i
done
