#!/bin/sh

for i in robotmaker robotcontroller robotshop; do
    oc import-image andrewazores/$i:latest --from=andrewazores/$i --confirm
done
