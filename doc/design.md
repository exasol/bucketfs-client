# Introduction

## Terms and Abbreviations

<dl>
    <dt>BFSC</dt><dd>BucketFS Client</dd>
</dl>

# Constraints

This section introduces technical system constraints.

# Solution Strategy

The BucketFS Client mimics the popular GNU core utilities in terms of user interface and behavior. It is designed to work independently of the platform so that it can be used on any OS that Java can run on and has a console (Linux, Mac OS X, Windows).

We use the [`bucketfs-java`](https://github.com/exasol/bucketfs-java) library to do the BucketFS-specific operations.

## Requirement Overview

Please refer to the [System Requirement Specification](system_requirements.md) for user-level requirements.

# Building Blocks

This section introduces the building blocks of the software. Together those building blocks make up the big picture of the software structure.

## `BucketFsClientApplication`

The `BucketFsClientApplication` is the central entry point and therefore responsible for creating all other required components.

Needs: impl


# Runtime

This section describes the runtime behavior of the software.

## Copying Files

### BucketFsUrl
`dsn~bucket-fs-url~1`

The `BucketFsUrl` represents the uniform resource locator for objects on BucketFs.

Covers:

* `req~bucketfs-url~1`

Needs: impl, utest

### CopyCommand Copies File From Public Bucket
`dsn~copy-command-copies-file-from-bucket~1`

The `CopyCommand` copies a single file from a public bucket to the local filesystem.

Covers:

* `req~copy-single-file-from-public-bucket~1`

Needs: impl, itest

### CopyCommand Copies File To Bucket With Interactive Password
`dsn~copy-command-copies-file-to-bucket~1`

The `CopyCommand` copies a single file from the local filesystem to a bucket.

Covers:

* `req~copy-single-file-to-bucket-with-interactive-password~1`

Needs: impl, itest

## Command Line Interface

### Sub-command Requires Hidden Write Password
`dsn~sub-command-requires-hidden-password~1`

In case of interactive password entry, sub-command (e.g. `cp`) tells the `picocli` library to prompt for a password with hidden entry.

Covers:

* `req~interactive-password-entry~1`

Needs: impl, itest

# Cross-cutting Concerns

# Design Decisions

## What CLI Interpreter Library do we Use?

For Java a vast number of CLI interpreter libraries exists.

To pick one we hold it against the following requirements:

1. Only the basic standard Java library as dependency
1. Supports Java 11
1. Supports GNU-style command line elements:
   * stand-alone parameters: `list`, `/home/fred/docs/foo.txt`
   * Single letter switch: `-c`
   * Chained single letter switches: `-ca`
   * Word switches:  `--word`,  `--multiple-words`
   * Switches with parameters: `--lines 3`, `--lines=3`
1. Supports sub-commands: `bfsc cp ...`
1. Supports positional parameters: `<from> [...] <to>`
1. Create "usage"
1. Create "help"
1. Actively developed
1. Available through Maven Central

The choice of the CLI library has an impact on usability and maintainability of the application.

### Alternatives considered

1. [args4J](http://args4j.kohsuke.org/). No dependencies. [Latest release from 2016](https://search.maven.org/artifact/args4j/args4j).
1. [JCommander](https://jcommander.org). Fulfills all criteria except for sub-commands and positional parameters, which are possible, but must be hand-coded. Leaner than the selected solution though.
1. [JOpt Simple](https://jopt-simple.github.io/jopt-simple). [Latest release from 2018](https://search.maven.org/artifact/net.sf.jopt-simple/jopt-simple) is in alpha stage. Latest stable from 2017.
1. [Spring Shell](https://github.com/spring-projects/spring-shell) has way too many dependencies. Latest release was from 2017.

### Decisions

We are using [picocli](https://picocli.info/). It is small enough, under active development, has no dependencies except the Java standard libraries and is well documented.

#### Command Line Parsing
`dsn~command-line-parsing~1`

BFSC uses [picocli](https://picocli.info/) to parse the command line.

Covers:

* `req~gnu-style-command-line-arguments~1`

Needs: impl

# Quality Scenarios

# Risks

# Acknowledgments

This document's section structure is derived from the "[arc42](https://arc42.org/)" architectural template by Dr. Gernot Starke, Dr. Peter Hruschka.