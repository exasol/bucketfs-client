# Introduction

## Terms and Abbreviations

<dl>
    <dt>BFSC</dt><dd>BucketFS Client</dd>
</dl>

# Constraints

This section introduces technical system constraints.

## No Confirmation for Delete Operation
`const~delete-ansynchronous~1`

Deleting a file in BucketFS is an asynchronous operation. Additionally BucketFS service does not signal whether the delete operation succeeded or failed.

Needs: dsn

## Ambigue Entries
`const~ambigue-entries~1`

A bucket can hold entries with common prefix and a slash `/` as separator. When interpreting this as a hierarchy similar to a file system, we need to consider that BucketFS also allows having a file with the same name as a directory. For example `ambigue/file.txt` and `ambigue` can exist at the same time.

When copying from the local file system to BucketFS this does not create any difficulty but an ambigue entry cannot be copied from BucketFS to the local file system as this allows an entry only to be either a file or a directory but not both at the same time.

Needs: dsn

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

### CopyCommand Copies File To Bucket With Write Password
`dsn~copy-command-copies-file-to-bucket~1`

The `CopyCommand` copies a single file from the local filesystem to a bucket.

Covers:
* `req~copy-single-file-to-bucket-with-interactive-password~2`

Needs: impl, itest

### Copying Ambigue Entries From Bucket to Local File System
`dsn~download-ambigue-file~1`

If the user requests to copy an ambigue entry from a bucket to the local file system non-recursively then BFSC copies only the regular file but not the directory with the same name.

BFSC downloads an ambigue regular file if all of the following conditions hold true
* The user requests to copy an ambigue entry from a bucket to the local file system
* The user does not request recursive mode

Covers
* `const~ambigue-entries~1`

Needs: impl, itest

`dsn~download-ambigue-directory-recursively~1`

BFSC recursively downloads an ambigue directory if all of the following conditions hold true
* The user requests to copy an ambigue entry from a bucket to the local file system.
* The user requests recursive mode.
* The user resolves the ambiguity by appending a slash to the name of the ambigue entry.

Covers
* `const~ambigue-entries~1`

Needs: impl, itest

`dsn~download-ambigue-entry-recursively~1`

BFSC reports an error if all of the following conditions hold true
* The user requests to copy an ambigue entry from a bucket to the local file system.
* The user requests recursive mode.
* The entry requested by the user does have a trailing a slash.

Covers
* `const~ambigue-entries~1`

Needs: impl, itest

`dsn~copy-ambigue-entrie-on-lower-level~1`

When copying recursively from a bucket to the local file system and BFSC detects an ambigue entry on a lower level then BFSC reports a warning and copies only the directory but not the regular file with the same name.

Covers
* `const~ambigue-entries~1`

Needs: impl, itest

## Listing Contents

### Listing Contents of a Bucket
`dsn~list-contents~1`

The `ListCommand` displays the contents of a directory in a bucket as a sorted list.

Covers:
* `req~list-contents-of-a-bucket~1`

Needs: impl, itest

### Files and Directories
`dsn~list-files-and-directories~1`

The output of the `ListCommand` contains files as well as directories if there are such.

Covers:
* `req~list-contents-of-a-bucket~1`

Needs: impl, itest

### Indicate the Type of Entrys
`dsn~highlight-type-of-entries~1`

The `ListCommand` indicates directories with a trailing slash.

Rationale:
* By appending a slash to directories BFSC enables users to distinguish ambigue entries in BucketFs, i.e. regular files and directories with identical names.

Covers:
* `req~list-contents-of-a-bucket~1`
* `const~ambigue-entries~1`

Needs: impl, itest

## Deleting Files

### Deleting a File in a Bucket
`dsn~delete-file~1`

The `DeleteCommand` deletes a file in a bucket.

Covers:
* `req~delete-files~1`

Needs: impl, itest

### No Error When Deleting a Non-Existing File
`dsn~no-error-when-deleting-a-non-existing-file~1`

BFSC does not signal an error in case of trying to delete a non-existing file.

Covers:
* `const~delete-ansynchronous~1`

Needs: impl, itest

### Deleting Ambigue Entries
`dsn~delete-ambigue-entry-non-recursively~1`

When the user asks to delete an ambigue entry in non-recursive mode then BFSC will only delete the regular file but not the directory with the same name.

Covers:
* `const~ambigue-entries~1`

Needs: impl, itest

`dsn~delete-ambigue-entry-recursively~1`

When the user asks to delete an ambigue entry in recursive mode then BFSC will delete
* the regular file
* the directory recursively

Covers:
* `const~ambigue-entries~1`

Needs: impl, itest

## Command Line Interface

### Sub-command Requires Hidden Write Password
`dsn~sub-command-requires-hidden-password~2`

In case of interactive password entry BFSC prompts for a password with hidden entry.

Covers:
* `req~no-command-line-option-for-password~1`
* `req~password-protected-bucket-access~1`

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
1. [JCommander](https://jcommander.org). Fulfills all criteria except for sub-commands and positional parameters, which are possible, but must be hand-coded. Leaner than the selected solution, though.
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