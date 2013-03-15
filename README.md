CloudBees SDK API and Driver

This repository defines two pieces.

One is the APIs that `bees` CLI commands would rely on, and their associated middle-tier services
to make command implementations simple and easy.

The other is the driver code, which is the actual entry point of the `bees` command. This houses
code that load commands and transfer executions.
