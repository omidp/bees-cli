# CloudBees SDK API and Driver

This repository defines two pieces.

One is the APIs that `bees` CLI commands would rely on, and their associated middle-tier services
to make command implementations simple and easy.

The other is the driver code, which is the actual entry point of the `bees` command. This houses
code that load commands and transfer executions.


Edit/Build/Debug cycle
-----------------------------
1. modify the code in IDE
2. run `mvn install` to build the source tree
3. run `BEES_OPTS=-Dbees.driverVersion=LATEST bees CMD OPT OPT OPT...` to run your newly built SDK API/Driver
