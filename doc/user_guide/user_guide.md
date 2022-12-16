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
      ```shell
      sudo apt install openjdk-11-jre-headless
      ```
    * RedHat
      ```shell
      sudo yum install java-11-openjdk
      ```
    * SuSE: [OpenJDK build of the Leap project](https://software.opensuse.org/download/package?package=java-11-openjdk&project=openSUSE%3ALeap%3A15.1%3AUpdate)

## Using the Client

The client is a Java program.

### Using the JAR File Directly

The purist way of starting that is of course starting the application straight out of the JAR archive.

```shell
java -jar "<path-to-bfsc-jar>" <command> <option> ...
```

Since this gets a little bit unwieldy very quickly, you should set an alias:

```shell
alias bfsc='java -jar <path-to-bfsc-jar>'
```

### Sub-commands and Command Line Options

Sub-commands control the action that BFSC is taking. For each type of action there is a sub-command. The way to use this is:

```shell
bfsc <sub-command> <option> ...
```

BFSC supports the following sub commands to inspect and manipulate files in the BucketFS:

| Command                | Description                                        |
|------------------------|--------------------------------------------------- |
| `bfsc ls`              | List #the contents of BucketFS                      |
| `bfsc ls bfs:/folder`  | List the contents of a directory in BucketFS       |
| `bfsc cp a.jar bfs:/`  | Upload a file from local fie system to BucketFS    |
| `bfsc cp bfs:/a.jar .` | Download a file from BucketFS to local file system |
| `bfsc rm /a.jar`       | Remove a file from BucketFS                        |

The following sections describe the commands in detail.

### General Concepts

#### BucketFS URLs

BFSC uses specific URLs to refer to files or paths in a bucket.

```
bfs[s]://<host>:<port>/<path-in-bucket>
```

The protocol is either `bfs` or `bfss` for a connection that is TLS secured.

You specify the host or IP address of the machine where the BucketFS service runs. Each service has its own port that you set in the BucketFS service configuration so a port number identifies the service.

The first path element is always the name of the bucket. The next elements are the path relative to that buckets root.

#### Password Protected Bucket Access

While buckets can be public for reading depending on their configuration, writing is always password protected. For write operations like copying files to the BucketFS or deleting files from the BucketFS BFSC will retrieve the required write password. BFSC supports to read the password either from the profile in the [configuration file](#configuration-file) or from an interactive prompt hiding the characters typed by the user.

BFSC does not support to provide the password on the command line to avoid the password showing up in the command history. As a general rule you should never put any credentials directly into a command line.

#### Retrieving the Password, Base64 Encoding

The password for write operations to the BucketFS is usually stored in file `/exa/etc/EXAConf`:
```
WritePass = <value>
```

Additionally the `<value>` is base64 encoded.  For additional convenience BFSC allows you to provide the password in base64 encoded format and let BFSC decode it, see section [Configuration File](#configuration-file). This applies to all methods providing the password: via the profile in your configuration file, as well as via interactive prompt.

#### Configuration File

Besides specifying the complete URL on the command line you can use fallbacks for some parts of the URL that apply to multiple operations.

BFSC uses the following precedence for URI parts
1. Supplied on the command line, or entered interactively in case of the [password](#password-protected-bucket-access)
2. Elements of your profile
3. Hard coded default value for host and port

You can define the profile in BFSC's configuration file in your home directory: `~/.bucketfs-client/config.ini`. On Windows the home directory is `%USERPROFILE%`.

The configuration file uses the INI-file syntax. It is devided into sections. Each section contains a number of lines defining a *profile*. The first line of each section specifies the name of the profile in brackets. The default profile's name is `default`. Each of the following lines of the section may assign a value to an *element*.

BFCS uses the following elements of your profile with the specified hard coded default values:

| Parameter                       | Profile Element | Default value |
|---------------------------------|-----------------|---------------|
| Host address of BucketFS server | `host`          | `localhost`   |
| Port                            | `port`          | `2580`        |
| Name of root bucket             | `bucket`        | (none)        |
| Write passsword                 | `password`      | (none)        |
| Decode base64 encoded password  | `decode-base64` | `false`       |

Here is an example for the content of a configuration file for BFSC:
```
[default]
host=1.2.3.4
port=8888
bucket=simba
password=abc
decode-base64=true
```

Using this configuration file the command line
```shell
bfsc cp foo.jar bfs:/drivers
```

... then is equivalent to the following command line without a configuration file:
```shell
bfsc cp foo.jar bfs://1.2.3.4:8888/simba/drivers/foo.jar
```

#### Name of the Root Bucket

BFSC cannot detect whether the URL on the command line contains a bucket name or directly starts with the path *inside* the bucket. If the profile in your configuration file specifies a bucket name, then BFSC will always prepend that bucket name to the path of the URL.

If you want to provide the name of the root bucket on the command line then please do not set `bucket` in your profile.

#### Configuration File

Besides specifying the complete URL on the command line you can use fallbacks for some parts of the URL that apply to multiple operations.

BFSC uses the following precedence for URI parts
1. Supplied on the command line, or entered interactively in case of the [password](#password-protected-bucket-access)
2. Elements of your profile
3. Hard coded default value for host and port

You can define the profile in BFSC's configuration file in your home directory: `~/.bucketfs-client/config`. On Windows the home directory is `%USERPROFILE%`.

The configuration file uses the INI-file syntax. It is devided into sections. Each section contains a number of lines defining a *profile*. The first line of each section specifies the name of the profile in brackets. The default profile's name is `default`. Each of the following lines of the section may assign a value to an *element*.

BFCS uses the following elements of your profile with the specified hard coded default values:

| Parameter                       | Profile Element | Default value |
|---------------------------------|-----------------|---------------|
| Host address of BucketFS server | `host`          | `localhost`   |
| Port                            | `port`          | `2580`        |
| Name of root bucket             | `bucket`        | (none)        |
| Write passsword                 | `password`      | (none)        |

Here is an example for the content of a configuration file for BFSC:
```
[default]
host=1.2.3.4
port=8888
bucket=simba
password=abc
```

Using this configuration file the command line
```shell
bfsc cp foo.jar bfs:/drivers
```

... then is equivalent to the following command line without a configuration file:
```shell
bfsc cp foo.jar bfs://1.2.3.4:8888/simba/drivers/foo.jar
```

#### Name of the Root Bucket

BFSC cannot detect whether the URL on the command line contains a bucket name or directly starts with the path *inside* the bucket. If the profile in your configuration file specifies a bucket name, then BFSC will always prepend that bucket name to the path of the URL.

If you want to provide the name of the root bucket on the command line then please do not set `bucket` in your profile.

### Bucket Operations

#### Copying Files

In the majority of all cases you will copy files _to_ a bucket. For example if you want to install a library that you plan to use in a Python or Java UDF.

```shell
bfsc cp <from> <to>
```

Example:

```shell
bfsc cp foo-driver-1.2.3.jar bfs://192.168.0.1:2580/default/drivers/foo-driver-1.2.3.jar
```

While the `cp` command can rename the copied file in the target location you can also omit the filename to tell BFSC to leave the filename unchanged just as the GNU `cp` command does:

```shell
bfsc cp foo.jar bfs:/drivers/
bfsc cp bfs:/drivers/foo.jar .
```

#### Listing Contents of a Bucket or a Directory in a Bucket

| Command            | Output                        |
|--------------------|-------------------------------|
| `bfsc ls`          | Contents of root bucket       |
| `bfsc ls folder/a` | Contents of folder `folder/a` |

Example output
```
a.txt
b.txt
folder\
```

Note that BFSC highlights folders with a trailing slash `/` to separate them from files potentially having the same name.

#### Deleting Files in from a Bucket
Examples
```shell
bfsc rm a.txt
bfsc rm folder/b.txt
```
