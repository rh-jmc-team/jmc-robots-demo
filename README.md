# jmc-robots-demo

## Local Run

Run `sh build.sh` then `sh run.sh` in the root of this project. This will run
the three applications in Docker containers running on the host system, and
additionally one [container-jfr](https://github.com/rh-jmc-team/container-jfr)
instance for instrumenting the three demo applications with JFR.

## Openshift (CodeReady Containers) Run

Install and configure [CodeReady Containers](https://github.com/minishift/minishift) and
ensure that your `oc` cli commands are working. `cd minishift` into the utility
script directory.

Then run `sh setup.sh`, which will create a new project named `robots`
containing an application corresponding to each of the three applications
within this project.

Next, run `sh client-run.sh` to start a `container-jfr` instance
within the CRC node, which can then be used interactively to instrument
each demo application using JFR. Alternatively, run
`sh run-daemon.sh` to run the `container-jfr` instance in
non-interactive mode (this will require `jq` to be installed on your local
system), and use `nc` to connect to the client afterwards.

`sh download-jfr.sh foo` can be used to download a recording named
`foo` from the `container-jfr` recording exporter. This will save to
`$PWD/foo.jfr`.

Finally, `sh update-images.sh` can be used to update the CRC
image registry with the latest application images from the upstream remote
Docker image repository.

For more information on how to use ContainerJFR to interact with the robot demo
applications, visit the `container-jfr`
[docs](https://github.com/rh-jmc-team/container-jfr).
