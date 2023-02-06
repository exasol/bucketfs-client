# BucketFS Client User Guide

The BucketFS Client (BFSC) is a client program for Exasol's BucketFS distributed file system.

BFSC allows you to read and write the contents of buckets.

A bucket is a storage space on an Exasol cluster that automatically takes care of distributing files loaded onto it. Those files can then be used in [User Defined Functions (UDFs)](https://docs.exasol.com/database_concepts/udf_scripts.htm), e.g. for [Virtual Schemas](https://docs.exasol.com/db/latest/database_concepts/virtual_schemas.htm) which are a special type of UDFs. See [official documentation](https://docs.exasol.com/db/latest/database_concepts/bucketfs/bucketfs.htm) for more details on BucketFS.

## Use Cases

The most common use case for BucketFS is storing files on it that UDFs need:

* Libraries for your UDFs
* Drivers for external data sources or sinks
* Configuration files
* Immutable lookup tables

## Using the Client

The client is implemented in Java and provides native executables for x86 CPU with operating system Ubuntu Linux, Mac, or Windows.

To make BFSC really handy you can either rename the binary or set an alias

```shell
alias bfsc='bfsc-linux_x86'
```

### Sub-commands and Command Line Options

Sub-commands control the action that BFSC is taking. For each type of action there is a sub-command. The way to use this is:

```shell
bfsc <sub-command> <option> ...
```

BFSC supports the following sub commands to inspect and manipulate files in the BucketFS. You can use a [configuration file](#configuration-file) or rely on default values to abbreviate URLs in BucketFS.

| Full                                           | Abbreviated           | Description                                        |
|------------------------------------------------|-----------------------|----------------------------------------------------|
| `bfsc ls bfs://localhost:2580/default`         |`bfsc ls`              | List the contents of BucketFS                      |
| `bfsc ls bfs://localhost:2580/default/folder`  |`bfsc ls folder`       | List the contents of a directory in BucketFS       |
| `bfsc cp a.jar bfs://localhost:2580/default/`  |`bfsc cp a.jar bfs:/`  | Upload a file from local fie system to BucketFS    |
| `bfsc cp bfs://localhost:2580/default/a.jar .` |`bfsc cp bfs:/a.jar .` | Download a file from BucketFS to local file system |
| `bfsc rm bfs://localhost:2580/a.jar`           |`bfsc rm /a.jar`       | Remove a file from BucketFS                        |

The following sections will first explain some general concepts and then describe each of the commands in detail.

## General Concepts

### BucketFS URLs

BFSC uses specific URLs to refer to files or paths in a bucket.

```
bfs[s]://<host>:<port>/<path-in-bucket>
```

The protocol is either `bfs` or `bfss` for a connection that is TLS secured.

You specify the host or IP address of the machine where the BucketFS service runs. Each service has its own port that you set in the BucketFS service configuration so a port number identifies the service.

The first path element is the name of the bucket. The next elements are the path relative to that bucket's root.

### Password Protected Bucket Access

There are separate passwords for read- and write-operations:

| Operation        | Examples                                                   | Password protection            |
|------------------|------------------------------------------------------------|--------------------------------|
| Write operations | Upload files to BucketFS, delete files from the BucketFS   | Always password protected      |
| Read operations  | List bucket contents, download files                       | Unprotected for public buckets |

BFSC supports to read required passwords either from the profile in the [configuration file](#configuration-file) or from an interactive prompt hiding the characters typed by the user.

BFSC does not support to provide passwords on the command line to avoid the password showing up in the command history. As a general rule you should never put any credentials directly into a command line.

BFSC will normally not ask for a read-password interactively.
* This is fine for public buckets as these are not password-protected.
* This is also fine if the password is contained in your profile.
* In case of accessing a private bucket without the read-password being contained in your profile you must add option `-pw` or `--require-read-password` to force BFSC to ask for the password via interactive prompt.

### Retrieving the Password

The passwords are usually stored in file `/exa/etc/EXAConf`:
```
WritePasswd = <value>
ReadPasswd = <value>
```

Please note that each of the passwords is base64-encoded. So before providing the passwords to BFSC please apply `echo <password> | base64 -d`.

### Configuration File

Besides specifying the complete URL on the command line you can use defaults for some parts of the URL.

BFSC uses the following precedence for URI parts
1. Supplied on the command line, or entered interactively in case of a [password](#password-protected-bucket-access)
2. Data in your profile
3. Hard coded default value for host and port

You can define the profile in BFSC's configuration file in your home directory: `~/.bucketfs-client/config.ini`. On Windows the home directory is `%USERPROFILE%`.

The configuration file uses the INI-file syntax. An INI file is divided into sections. Each section contains a number of lines defining a *profile*. The first line of each section specifies the name of the profile in brackets. The default profile's name is `default`. Each of the following lines of the section may assign a value to a variable.

BFCS uses the following elements of your profile with the specified hard coded default values:

| Parameter                       | Variable         | Default value |
|---------------------------------|------------------|---------------|
| Host address of BucketFS server | `host`           | `localhost`   |
| Port                            | `port`           | `2580`        |
| Name of root bucket             | `bucket`         | (none)        |
| Password for read operations    | `password.read`  | (none)        |
| Password for write operations   | `password.write` | (none)        |

Here is an example for the content of a configuration file for BFSC:
```
[default]
host=1.2.3.4
port=8888
bucket=simba
password.read=abc
password.write=def
```

Using this configuration file the command line
```shell
bfsc cp foo.jar bfs://1.2.3.4:8888/simba/drivers/foo.jar
```
... can then be abbreviated to:
```shell
bfsc cp foo.jar bfs:/drivers
```

### Name of the Root Bucket

BFSC cannot detect whether the URL on the command line contains a bucket name or directly starts with the path *inside* the bucket. If the profile in your configuration file specifies a bucket name, then BFSC will always prepend that bucket name to the path of the URL.

If you want to provide the name of the root bucket on the command line then please do not set `bucket` in your profile.

In case your profile specifes the name of the root bucket all of the following commands will list the contents of directory `folder` in the root bucket:
```shell
bfsc ls folder
bfsc ls /folder
bfsc ls bfs:///folder
bfsc ls bfs:/folder
```

In case your profile does not specify the name of the root bucket all of the following commands will list the contents of root bucket `default`:

```shell
bfsc ls /default/
bfsc ls bfs:/default/
bfsc ls bfs:///default/
```

## Bucket Operations

### Listing Contents of a Bucket or a Directory in a Bucket

With `bfsc ls` you can list the contents of a directory in the bucket. If you do not specify a directory then the `ls` command will list all files and directories in the bucket on top level.

| Command (abbreviated) | Output                                  |
|-----------------------|-----------------------------------------|
| `bfsc ls`             | Contents of the bucket's root directory |
| `bfsc ls folder`      | Contents of directory `folder`          |

Example output
```
a.txt
b.txt
folder/
```

BFSC appends a trailing slash `/` to separate directories from files potentially having the same name.

If you want to list all *buckets* rather than the *contents* of a specific bucket, then please ensure to not not set `bucket` in your profile.

Example output for listing root buckets
```
default
simba
```

With option `-r` or `--recursive` you can obtain a recursive listing of all contents inside a directory of a bucket. However when listing *buckets* then BFSC will ignore option `-r`.

Example (abbreviated):
```shell
$ bfsc ls -r folder
a.txt
b.txt
sub/a1.txt
```

### Copying Files

With `bfsc cp` you can copy files between BucketFS and the local file system.

```shell
bfsc cp <from> <to>
```

In the majority of all cases you will copy files _to_ a bucket. For example if you want to install a library that you plan to use in a Python or Java UDF (User Defined Function):

```shell
bfsc cp foo-driver-1.2.3.jar bfs://192.168.0.1:2580/default/drivers/foo-driver-1.2.3.jar
```

While the `cp` command can rename the copied file in the target location you can also omit the filename to tell BFSC to leave the filename unchanged just as the GNU `cp` command does:

```shell
bfsc cp foo.jar bfs:/drivers/
bfsc cp bfs:/drivers/foo.jar .
```

Command `cp` supports recursive copying:

```shell
bfsc cp -r Dir bfs:/
```

However there are some special cases explained in the following sections.

#### Ambigue Entries in BucketFS

BucketFS differs from the local file system as it may contain *ambigue* entries. An ambigue entry represents a regular file and a directory at the same time. Obviously you cannot download both flavors of such an entry to the local file system and BFSC will abort with an error message.

You can however tell BFSC to download *one* of the flavors of such an ambigue entry.
* For recursively downloading the directory please specify option `-r` *and* append a slash `/` to the name of the ambigue entry.
* For downloading the regular file omit both of them.

| Command (abbreviated)      | Semantics                                |
|----------------------------|------------------------------------------|
| `bfsc cp bfs:/ambigue/`    | error message                            |
| `bfsc cp -r bfs:/ambigue`  | error message                            |
| `bfsc cp bfs:/ambigue`     | download regular file `ambigue`          |
| `bfsc cp -r bfs:/ambigue/` | recursively download directory `ambigue` |

For non-ambigue entries the trailing slash is not relevant.

For ambigue entries on lower levels during recursive download BFSC will report a warning and only download the directory flavor of the ambigue entry.

#### Existing Entries in the Local File System

When downloading from BucketFS the target in the local file system might already exist and could be either a directory or a regular file.

The following table shows all cases assuming the local file system contains a regular file `b.txt` and a directory `B`.

| Command (abbreviated)  | Semantics / error message                              |
|------------------------|--------------------------------------------------------|
| `cp bfs://a.txt b.txt` | download regular file `a.txt` and rename it to `b.txt` |
| `cp bfs://a.txt B`     | download regular file `a.txt` into existing local directory `B` and keep the original filename resulting in `B/a.txt` |
| `cp -r bfs://A B`      | recursively download directory `A` to `B/A`            |
| `cp -r bfs://A b.txt`  | error message                                          |

When downloading a directory from BucketFS you can rename the directory by specifying a local name that does not exist, yet.

### Deleting Files From a Bucket

With `bfsc rm` you can delete files in BucketFS.

Examples (abbreviated):
```shell
bfsc rm a.txt
bfsc rm folder/b.txt
bfsc rm -r folder
```

As for the other commands command `rm` supports recursive removal, too. However there are some special remarks
* BFSC will not display an error message if you specify `-r` for deleting a regular file.
* With option `-r` BFSC will remove files, directories, and ambigue entries.
* Applying `rm` without option `-r` on an ambigue entry will remove only the regular file but not the directory with the same name.
