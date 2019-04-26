# jmc-robots-demo

## Local Run

Run `sh build.sh` then `sh run.sh` in the root of this project. This will run
the three applications in Docker containers running on the host system, and
additionally one [container-jmc](https://github.com/andrewazores/container-jmc)
instance for instrumenting the three demo applications with JFR.

## Openshift (Minishift) Run

Install and configure [Minishift](https://github.com/minishift/minishift) and
ensure that your `oc` cli commands are working. `cd minishift` into the utility
script directory.

Then run `sh setup.sh`, which will create a new project named `robots`
containing an application corresponding to each of the three applications
within this project.

Next, run `sh client-run.sh` to start a `container-jmc` instance
within the Minishift node, which can then be used interactively to instrument
each demo application using JFR. Alternatively, run
`sh run-daemon.sh` to run the `container-jmc` instance in
non-interactive mode (this will require `jq` to be installed on your local
system), and use `nc` to connect to the client afterwards.

`sh download-jfr.sh foo` can be used to download a recording named
`foo` from the `container-jmc` recording exporter. This will save to
`$PWD/foo.jfr`.

Finally, `sh update-images.sh` can be used to update the Minishift
image registry with the latest application images from the upstream remote
Docker image repository.

## Connecting to Robot Applications from `container-jmc`

From within the `container-jmc` "shell", each of the Robot applications can be
reached via the hostnames `robotmaker`, `robotcontroller`, and `robotshop`.
Use `connect robotmaker` to connect the `container-jmc` client to the
RobotMaker application, for example. For more information on what to do from
that point, visit the `container-jmc`
[docs](https://github.com/andrewazores/container-jmc).
