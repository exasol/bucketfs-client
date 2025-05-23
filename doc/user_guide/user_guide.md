# BucketFS Client User Guide

The BucketFS Client (BFSC) is a client program for Exasol's BucketFS distributed file system.

BFSC allows you to read and write the contents of buckets.

A bucket is a storage space on an Exasol cluster that automatically takes care of distributing files loaded onto it. Those files can then be used in [User Defined Functions (UDFs)](https://docs.exasol.com/database_concepts/udf_scripts.htm). An example would be [Virtual Schemas](https://docs.exasol.com/db/latest/database_concepts/virtual_schemas.htm) which are a special type of UDFs. See [official documentation](https://docs.exasol.com/db/latest/database_concepts/bucketfs/bucketfs.htm) for more details on BucketFS.

## Use Cases

The most common use case for BucketFS is storing files on it that UDFs need:

* Libraries for your UDFs
* Drivers for external data sources or sinks
* Configuration files
* Immutable lookup tables

## Using the Client

The client is implemented in Java and provides native executables for x86 CPU with operating system Ubuntu Linux or Windows.

#### Supported Platforms

| Platform | Available?   | Notes |
|----------|--------------|-------|
| Linux    | &#x2705; yes | ¹⁾    | 
| Windows  | &#x2705; yes | ²⁾    | 
| macOS    | &#x274c; no  | ³⁾    |

¹⁾ Release 2.2.1 is tested on Ubuntu 24.04.

²⁾ When downloading the native executable, some browsers may display a warning about security risks imposed by downloading unsigned binaries.

³⁾ Latest macOS Ventura (e.g. version 13.2.1) refuses to execute unsigned binaries.

Signed binaries for Windows and macOS will be provided as soon as a signing process is established.

For all platforms not supported currently or in case you do not want to execute an unsigned binary on your platform, please refer to the [jar file](#running-the-jar-file) provided with each release.

To make using the BFSC more convenient, you can either rename the binary or set an alias

```shell
alias bfsc='bfsc-linux_x86'
```

### Running the JAR File

Besides executing the binaries, you can also download BFSC's JAR file and let a Java VM execute it.

For that you need Java 17 or later, the installation procedure depends on your operating system.

* Any operating system: [Adoptium OpenJDK build](https://adoptium.net/)
* Linux
    * Ubuntu, Debian:
      ```shell
      sudo apt install openjdk-17-jre-headless
      ```
    * RedHat
      ```shell
      sudo yum install java-17-openjdk
      ```
    * SuSE: [OpenJDK build of the Leap project](https://software.opensuse.org/download/package?package=java-17-openjdk&project=openSUSE%3AFactory)

When Java 17 is available, you can run BFSC's JAR file with the following command:

```shell
java -jar "<path-to-bfsc-jar>" <command> <option> ...
```

An alias in your shell is even more useful here:

```shell
alias bfsc='java -jar <path-to-bfsc-jar>'
```

### Sub-commands and Command Line Options

Sub-commands control the action that BFSC is taking. For each type of action there is a sub-command. The way to use this is:

```shell
bfsc <sub-command> <option> ...
```

BFSC supports the following sub commands to inspect and manipulate files in the BucketFS.

| Command                                        | Description                                        |
|------------------------------------------------|----------------------------------------------------|
| `bfsc ls bfs://localhost:2580/default/`        | List the contents of BucketFS                      |
| `bfsc ls bfs://localhost:2580/default/folder/` | List the contents of a directory in BucketFS       |
| `bfsc cp a.jar bfs://localhost:2580/default/`  | Upload a file from local fie system to BucketFS    |
| `bfsc cp bfs://localhost:2580/default/a.jar .` | Download a file from BucketFS to local file system |
| `bfsc rm bfs://localhost:2580/a.jar`           | Remove a file from BucketFS                        |

The following sections will first explain some general concepts and then describe each of the commands in detail.

## General Concepts

### BucketFS URLs

BFSC uses specific URLs to refer to files or paths in a bucket.

```
bfs[s]://<host>:<port>/[<path-in-bucket>]
```

The protocol is either `bfs` or `bfss` for a connection that is TLS secured.

You specify the host or IP address of the machine where the BucketFS service runs. Each service has its own port that you set in the BucketFS service configuration, so a port number identifies the service.

The first path element is the name of the bucket. The next elements are the path relative to that bucket's root.

### Password Protected Bucket Access

There are separate passwords for read- and write-operations:

| Operation        | Examples                                                   | Password protection            |
|------------------|------------------------------------------------------------|--------------------------------|
| Write operations | Upload files to BucketFS, delete files from the BucketFS   | Always password protected      |
| Read operations  | List bucket contents, download files                       | Unprotected for public buckets |

BFSC supports reading required passwords either from the profile in the [configuration file](#configuration-file) or from an interactive prompt hiding the characters typed by the user.

BFSC does not support passwords on the command line to avoid the password showing up in the command history. As a general rule, you should never put any credentials directly into a command line.

BFSC will normally not ask for a read-password interactively.
* This is fine for public buckets as these are not password-protected.
* This is also fine if the password is contained in your profile.
* In case of accessing a private bucket without the read-password being contained in your profile you must add option `-pw` or `--require-read-password` to force BFSC to ask for the password via interactive prompt.

### Retrieving the Password

#### Variant 1: Reading the Password Using `confd_client` (Recommended)

You can get the configuration of a BucketFS service with the `confd_client` tool (see also ["bucketfs_info"](https://docs.exasol.com/db/latest/confd/jobs/bucketfs_info.htm)):

Here is an example of getting the decoded read-password. Replace the BucketFS service name (here `bfsdefault`) if necessary.

```shell
confd_client bucketfs_info bucketfs_name: bfsdefault | grep -oP 'read_passwd: \K.*' | base64 -d
```

And for the write-password:

```shell
confd_client bucketfs_info bucketfs_name: bfsdefault | grep -oP 'write_passwd: \K.*' | base64 -d
```

#### Variant 2: Reading the Password Directly From the EXAConf

Please note that this variant requires filesystem access to the Exasol database. The passwords are usually stored in file `/exa/etc/EXAConf`:
```
WritePasswd = <value>
ReadPasswd = <value>
```

Please note that each of the passwords is base64-encoded. So before providing the passwords to BFSC please apply `echo <password> | base64 -d`.

### Using TLS

When your BucketFS server uses TLS (HTTPS), use protocol `bfss`. On the command line specify the bucket URL as `bfss://<host>:<port>/<path-in-bucket>`. When using a [configuration file](#configuration-file), add option `protocol = bfss` to the profile.

If the BucketFS server uses a self-signed certificate or the certificate is not available in the Java truststore, the connection will fail during certificate validation. To solve this, store the certificate as a file and specify it with command line option `-c` / `--certificate` or with option `certificate` in the profile.

### Configuration File

You can define the profile in BFSC's configuration file in your home directory: `~/.bucketfs-client/config.ini`. On Windows the home directory is `%USERPROFILE%`.

The configuration file uses the INI-file syntax. An INI file is divided into sections. Each section contains a number of lines defining a *profile*. The first line of each section specifies the name of the profile in brackets. The default profile's name is `default`. Each line after the section header assigns a value to a variable.

BFSC uses the following elements of your profile with the specified hard-coded default values:

| Parameter                       | Variable         | Default value                   |
|---------------------------------|------------------|---------------------------------|
| Name of root bucket             | `bucket`         | (none)                          |
| Password for read operations    | `password.read`  | (none)                          |
| Password for write operations   | `password.write` | (none)                          |
| Path to TLS certificate         | `certificate`    | (none)                          |

Here is an example content of a configuration file for BFSC:
```
[default]
bucket=simba
password.read=abc
password.write=def
certificate=/path/to/cert.crt
```

### Name of the Root Bucket

BFSC cannot detect whether the URL on the command line contains a bucket name or directly starts with the path *inside* the bucket. If the profile in your configuration file specifies a bucket name, then BFSC will always prepend that bucket name to the path of the URL.

If you want to provide the name of the root bucket on the command line then please do not set `bucket` in your profile.

In case your profile specifies the name of the root bucket all following commands will list the contents of directory `folder` in the root bucket:

```shell
bfsc ls folder
bfsc ls /folder
bfsc ls bfs:///folder
bfsc ls bfs:/folder
```

In case your profile does not specify the name of the root bucket all following commands will list the contents of root bucket `default`:

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

If you want to list all *buckets* rather than the *contents* of a specific bucket, then please ensure to not set `bucket` in your profile.

Example output for listing root buckets
```
default
simba
```

With option `-r` or `--recursive` you can get a recursive listing of all contents inside a directory of a bucket. However, when listing *buckets* then BFSC will ignore option `-r`.

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

In the majority of all cases you will copy files _to_ a bucket. For example, if you want to install a library that you plan to use in a Python or Java UDF (User Defined Function):

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

However, there are some special cases explained in the following sections.

#### Ambiguous Entries in BucketFS

BucketFS differs from the local file system as it may contain *ambiguous* entries. An ambiguous entry represents a regular file and a directory at the same time. You cannot download both flavors of such an entry to the local file system, and BFSC will abort with an error message.

You can, however, tell BFSC to download *one* of the flavors of such an ambiguous entry.
* For recursively downloading the directory please specify option `-r` *and* append a slash `/` to the name of the ambiguous entry.
* For downloading the regular file, omit both of them.

| Command (abbreviated)        | Semantics                                  |
|------------------------------|--------------------------------------------|
| `bfsc cp bfs:/ambiguous/`    | error message                              |
| `bfsc cp -r bfs:/ambiguous`  | error message                              |
| `bfsc cp bfs:/ambiguous`     | download regular file `ambiguous`          |
| `bfsc cp -r bfs:/ambiguous/` | recursively download directory `ambiguous` |

For non-ambiguous entries the trailing slash is not relevant.

For ambiguous entries on lower levels during recursive download, BFSC will report a warning and only download the directory flavor of the ambiguous entry.

#### Existing Entries in the Local File System

When downloading from BucketFS, the target in the local file system might already exist and could be either a directory or a regular file.

The following table shows all cases assuming the local file system contains a regular file `b.txt` and a directory `B`.

| Command (abbreviated)  | Semantics / error message                                                                                             |
|------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `cp bfs://a.txt b.txt` | download regular file `a.txt` and rename it to `b.txt`                                                                |
| `cp bfs://a.txt B`     | download regular file `a.txt` into existing local directory `B` and keep the original filename resulting in `B/a.txt` |
| `cp -r bfs://A B`      | recursively download directory `A` to `B/A`                                                                           |
| `cp -r bfs://A b.txt`  | error message                                                                                                         |

When downloading a directory from BucketFS, you can rename the directory by specifying a local name that does not exist, yet.

### Deleting Files From a Bucket

With `bfsc rm` you can delete files in BucketFS.

Examples (abbreviated):
```shell
bfsc rm a.txt
bfsc rm folder/b.txt
bfsc rm -r folder
```

As for the other commands command `rm` supports recursive removal, too. However, there are some special remarks
* BFSC will not display an error message if you specify `-r` for deleting a regular file.
* With option `-r` BFSC will remove files, directories, and ambiguous entries.
* Applying `rm` without option `-r` on an ambiguous entry will remove only the regular file but not the directory with the same name.

## Questions and Answers (Q&A)

### What are the BucketFS Default Values?

A standard Exasol installation has a default BucketFS service with a single bucket. Here are the parameters for this default setup.

| Parameter      | Default Value |
|----------------|---------------|
| Service name   | `bfsdefault`  |
| BFS HTTP port  | 2580          |
| BFS HTTPS port | 2581          |
| Bucket         | `default`     |

### How can I Experiment Safely With BucketFS?

If you want to familiarize yourself with BucketFS on your local machine, try Exasol's `docker-db`. Here is a command that starts a `docker-db` container and forwards the ports to your `localhost`.

```shell
docker run --name exasoldb \
 -p 127.0.0.1:8563:8563  -p 127.0.0.1:2580:2580 -p 127.0.0.1:2581:2581 \
 -p 127.0.0.1:443:443 -p 127.0.0.1:2222:22 \
 --detach --privileged --stop-timeout 120  exasol/docker-db:latest
```

This forwards the database port (8563), BFS (2580) and BFSS (2581), HTTPS (443) and SSH (22 → 2222).

If you want to extract the read / write passwords for the buckets, use `docker exec` to enter the container.

You can use `openssl s_client` to take a look at the self-signed certificate that the `docker-db` presents:

```shell
openssl s_client -connect 127.0.0.1:2581 -showcerts </dev/null
```

Note that while this method is good enough for local experiments with `docker-db`, you should **never trust** a certificate presented by a server process without verification.

```shell
docker exec -it <docker-container-id> bash
```

Then follow the steps described in ["Password-Protected Bucket Access"](#password-protected-bucket-access).

To verify that you have the right certificate stored (e.g., in `~/.bucketfs-client/server-cert.pub`) you can use `curl` and list the bucket contents:

```shell
curl --resolve exacluster.local:2581:127.0.0.1 --cacert ~/.bucketfs-client/server-cert.pub https://r:<read-password>@exacluster.local:2581
```

The self-signed certificate of the `docker-db` instance uses `exacluster.local` as hostname. Since the hostname we give `curl` must match the one in the certificate, we need to help with resolving that host to the local IP.

If the line above outputs the bucket name `default`, you are all set up.

### Where do I Find the TLS Certificate That the BucketFS Server uses?

The certificate is by default located under `/exa/etc/ssl/ssl.crt`.

Please note that everytime you start a fresh `docker-db`, you get a new self-signed certificate. Please take that into account when you experiment with BucketFS.