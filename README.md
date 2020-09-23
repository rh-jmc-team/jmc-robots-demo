# jmc-robots-demo

## Local Run

Run `sh build.sh` then `sh run.sh` in the root of this project. This will run
the three applications in Docker containers running on the host system, and
additionally one [container-jfr](https://github.com/rh-jmc-team/container-jfr)
instance for instrumenting the three demo applications with JFR.

## Openshift (CodeReady Containers) Run

Install and configure [CodeReady Containers](https://github.com/minishift/minishift) and
ensure that your `oc` cli commands are working. 

Then run `sh openshift/setup-robots.sh`, which will create a new project named
`robots` containing an application corresponding to each of the three
applications within this project.

Then deploy ContainerJFR to the namespace. The easiest way to achieve this is
to clone the [operator](https://github.com/rh-jmc-team/container-jfr-operator)
and run `make deploy` (no build is required - an image from the Quay.io remote
registry will be pulled by your CRC node and deployed).

For more information on how to use ContainerJFR to interact with the robot demo
applications, visit the `container-jfr`
[docs](https://github.com/rh-jmc-team/container-jfr).
