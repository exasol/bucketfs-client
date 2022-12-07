# BucketFS Client User Guide

The BucketFS Client (BFSC) is a client program for Exasol's BucketFS distributed file system.

BFSC allows you to read and write the contents of buckets.

A bucket is a storage space on an Exasol cluster that automatically takes care of distributing files loaded onto it, see [official documentation](https://docs.exasol.com/db/latest/database_concepts/bucketfs/bucketfs.htm) for details.

Those files can then be used in [User Defined Functions (UDFs)](https://docs.exasol.com/database_concepts/udf_scripts.htm), e.g. for [Virtual Schemas](https://docs.exasol.com/db/latest/database_concepts/virtual_schemas.htm) which are a special type of UDFs.

## Use Cases

The most common use for BucketFS is storing files on it that UDFs need:

* Libraries for your UDFs
* Drivers for external data sources or sinks
* Configuration files
* Immutable lookup tables

## Prerequisites

To run the BucketFS Client, you need Java 11 or later.

Installation depends on your operating system.

* Any operating system: [Adoptium OpenJDK build](https://adoptium.net/)
* Linux
    * Ubuntu, Debian:
      ```bash
      sudo apt install openjdk-11-jre-headless
      ```
    * RedHat
      ```bash
      sudo yum install java-11-openjdk
      ```
    * SuSE: [OpenJDK build of the Leap project](https://software.opensuse.org/download/package?package=java-11-openjdk&project=openSUSE%3ALeap%3A15.1%3AUpdate)
* Windows: [Microsoft OpenJDK build](https://www.microsoft.com/openjdk)

## Using the Client

The client is a Java program.

### Using the JAR File Directly

The purist way of starting that is of course starting the application straight out of the JAR archive.

```bash
java -jar "<path-to-bfsc-jar>" <command> <option> ...
```

Since this gets a little bit unwieldy very quickly, you should set an alias:

```bash
alias bfsc='java -jar <path-to-bfsc-jar>'
```

### Sub-commands and Command Line Options

Sub-commands control the action that BFSC is taking. For each type of action there is a sub-command. The way to use this is:

```bash
bfsc <sub-command> <option> ...
```

BFSC supports the following sub commands to inspect and manipulate files in the BucketFS:

| Command                | Description                                        |
|------------------------|--------------------------------------------------- |
| `bfsc ls`              | List contents of BucketFS                          |
| `bfsc cp a.jar bfs:/`  | Upload file from local fie system to BucketFS      |
| `bfsc cp bfs:/a.jar .` | Download a file from BucketFS to local file system |
| `bfsc rm /a.jar`       | Remove file from BucketFS                          |

The following sections describe the commands in detail.

### General Concepts

#### BucketFS URLs

BFSC uses specific URL to refer to files or paths in a bucket.

```
bfs[s]://<host>:<port>/<bucket>/<path-in-bucket>
```

The protocol is either `bfs` or `bfss` for a connection that is TLS secured.

You specify the host or IP address of the machine where the BucketFS service runs. Each service has its own port that you set in the BucketFS service configuration so a port number identifies the service.

The first path element is always the name of the bucket. The next elements are the path relative to that buckets root.

#### Password Protected Bucket Access

While buckets can be public for reading depending on their configuration, writing is always password protected. For write operations like copying files to the BucketFS or deleting files from the BucketFS BFSC will retrieve the required write password. BFSC supports to read the password either from an [environment variable](#environment-variables-for-default-parameters) or from an interactive prompt hiding the characters typed by the user.

BFSC does not support to provide the password on the command line to avoid the password showing up in the command history. As a general rule you should never put any credentials directly in to a command line.

#### Environment Variables for Default Parameters

BFSC supports the following environment variables for applying parameters to all subsequent commands. If an environment variable is unset then BFSC uses the corresponding default value shown in the table below. Parameters supplied on the commandline will override the environment variables.

| Parameter                       | Environment variable | Default value |
|---------------------------------|----------------------|---------------|
| Host address of BucketFS server | `BUCKETFS_HOST`      | `localhost`   |
| Port                            | `BUCKETFS_PORT`      | `2580`        |
| Write passsword                 | `BUCKETFS_PASSWORD`  | (none)        |
| Name of root bucket             | `BUCKETFS_BUCKET`    | `default`     |

Example
```bash
BUCKETFS_PASSWORD=abc
BUCKETFS_BUCKET=simba

bfsc cp foo.jar bfs:drivers
```

Is identical to
```bash
bfsc cp foo.jar bfs://localhost:2580/simba/drivers/foo.jar
```

#### Retriving the Password, Base64 Encoding

The password for write operations to the BucketFS is usually stored in file `/exa/etc/EXAConf`:
```
WritePass = <value>
```

Additionally the `<value>` is base64 encoded.  For additional convenience BFSC allows you to provide the password in base64 encoded format and let BFSC decode it with commandline flag `--decode-base64-password` or `-d`. This applies to all methods providing the password: via environment `BUCKETFS_PASSWORD`, as well as via interactive prompt.

```bash
bfsc cp -d a.txt bfs:/
```

### Bucket Operations

#### Copying Files

In the majority of all cases you will copy files _to_ a bucket. For example if you want to install a library that you plan to use in a Python or Java UDF.

```bash
bfsc cp <from> <to>
```

Example:

```bash
bfsc cp foo-driver-1.2.3.jar bfs://192.168.0.1:2580/default/drivers/foo-driver-1.2.3.jar
```

While the `cp` command can rename the copied file in the target location you can also omit the filename to tell BFSC to leave the filename unchanged just as the GNU `cp` command does:

```bash
bfsc cp foo.jar bfs:drivers/
bfsc cp bfs:drivers/foo.jar .
```
