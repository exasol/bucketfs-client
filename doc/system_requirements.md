# System Requirement Specification BucketFS Java

## Introduction

The BucketFS Client (BFSC) allows users to handle Exasol's [BucketFS](https://docs.exasol.com/database_concepts/bucketfs/bucketfs.htm) similar to how they would work with a remote file system.

It offers a command line interface which is intentionally aligned with selected tools from the widely established [GNU Core Utilities](https://www.gnu.org/software/coreutils/coreutils.html).

If you know how to copy files using GNU `cp`, you will find the command line very familiar.

## About This Document

### Target Audience

The target audience are Exasol users. See section ["Stakeholders"](#stakeholders) for more details.

### Goal

The goal of BucketFS Java is to make programmatic access to BucketFS available while removing the necessity to know about the underlying mechanisms.

### Quality Goals

BFSJ main quality goals are in descending order of importance:

1. Compact client code

## Stakeholders

### Exasol users

This specification reflects the needs of Exasol users when interacting with BucketFS.

### Terms and Abbreviations

The following list gives you an overview of terms and abbreviations commonly used in BFSC documents.

* Bucket: container for files inside of BucketFS. Buckets can have individual access restrictions.
* BucketFS: service provided by the Exasol database that allows keeping files distributed across all data nodes of an Exasol cluster.

## Features

### Command Line Interface
`feat~command-line-interface~1`

BFSC can be controlled from the command line.

Rationale:

This allows both interactive use and integration into scripts. It also makes writing tutorials easy, since users can simply copy the commands to the console.

Needs: req

### Copying Files
`feat~copying-files~1`

BFSC supports copying files from and to buckets in BucketFS.

Rationale:

This allows uploading UFDs, configuration files and drivers, or checking the contents of files on BucketFS.

Needs: req

### Listing Contents
`feat~listing-contents~1`

BFSC supports listing the contents of a bucket in BucketFS.

Needs: req

### Deleting Files
`feat~deleting-files~1`

BFSC supports deleting files inside a bucket in BucketFS.

Needs: req

## Functional Requirements

### BucketFS URLs

To make BucketFS access similar to other file systems, BFSC introduces the concept of a BucketFS URL. This URL serves to identify resources inside a bucket.

#### BucketFS URL
`req~bucketfs-url~2`

A Bucket URL locates a resource inside a bucket with the following syntax:

```
bucket-url = protocol-identifier "://" bucketfs-service-name "/" bucket-name path-in-bucket

protocol-identifier = ("bfs" / "bfss")

bucketfs-service-name = segment

bucket-name = segment

path-in-bucket = 1*("/" segment)

segment = segment-start-character *segment-character

segment-start-character = (ALPHA / DIGIT / "_")

segment-character = (ALPHA / DIGIT / "-" / "_" / ".")
```

Covers:

* [feat~copying-files~1](#copying-files)

Needs: dsn

### TLS Support
`req~tls-support~1`

Users can manage copy files, list content and delete files with a BucketFS service that uses TLS encryption.

Needs: dsn

Covers:

* [`feat~copying-files~1`](#copying-files)
* [`feat~listing-contents~1`](#listing-contents)
* [`feat~deleting-files~1`](#deleting-files)

### Copying Files

#### Copy Single File from Public Bucket
`req~copy-single-file-from-public-bucket~1`

Users can copy a single file from BucketFS to local file storage with the following command:

```bash
bfs cp  <bfs-url> <local-path>
```

Covers:

* [feat~copying-files~1](#copying-files)

Needs: dsn

#### Copy Single File to Bucket With Interactive Password
`req~copy-single-file-to-bucket-with-interactive-password~2`

Users can copy a single file from local file storage to a public bucket with the following command:

```bash
bfs cp <local-path> <bfs-url>
```

Comment:

See also: [Password Protected Bucket Access](#password-protected-bucket-access)

Covers:

* [feat~copying-files~1](#copying-files)

Needs: dsn

### Listing Contents
`req~list-contents-of-a-bucket~1`

Users can list the contents of a bucket in BucketFS.

Covers:

* `feat~listing-contents~1`

Needs: dsn

### Deleting Files
`req~delete-files~1`

Users can delete files in a bucket in BucketFS.

Covers:

* `feat~deleting-files~1`

Needs: dsn

### Command Line Interface

BFSC is a command line tool. GNU tools like `cp` and `ls` serve as template for the arguments, mostly because a lot of users are familiar with them.

We considered also aligning the error messages, but experiments showed that this is not ideal. For one, working with BucketFS can cause errors that either are slightly different in cause than their counterparts in the GNU tools and on the other hand the error message coming from the GNU tools tend to be cryptic in some cases, probably for historical reasons. We want to improve the user experience in that particular point.

#### GNU-style Command Line Arguments
`req~gnu-style-command-line-arguments~1`

BFSC supports command line arguments that follow the [GNU command line style](https://www.gnu.org/software/libc/manual/html_node/Argument-Syntax.html).

Rationale:

This style is well established and users are accustomed to using it. It is supported by a large number of popular CLI tools.

Covers:

* `feat~command-line-interface~1`

Needs: dsn

#### Password Protected Bucket Access
`req~password-protected-bucket-access~1`

For write operations like copying files to the BucketFS or deleting files from the BucketFS BFSC will retrieve the required write password. BFSC supports to read the password either from an environment variable or from an interactive prompt hiding the characters typed by the user.

Covers:
* `feat~command-line-interface~1`

Needs: dsn

#### No Password on Command Line
`req~no-command-line-option-for-password~1`

BFSC does not enable users to supply the write password on the command line.

Covers:
* `feat~command-line-interface~1`

Rationale:

Passwords should never be supplied via the command line because they otherwise are logged in the command history. This is not the case with interactive input.

Needs: dsn
