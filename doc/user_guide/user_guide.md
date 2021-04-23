# BucketFS Client User Guide

The BucketFS Client (BFSC) is a client program for Exasol's BucketFS distributed file system.

It allows you to read and write the contents of buckets.

A bucket is a storage space on an Exasol cluster that automatically takes care of distributing files loaded onto it.

Those objects can then be used in [User Defined Functions (UDFs)](https://docs.exasol.com/database_concepts/udf_scripts.htm).

## Use Cases

The most common use for BucketFS is storing files on it that UDFs need:

* Libraries for your UDFs
* Drivers for external data sources or sinks
* Configuration files
* Immutable lookup tables

## Using the Client

The client is a Java program.

### Using the JAR File Directly

 The purist way of starting that is of course starting the application straight out of the JAR archive.

```bash
java -cp "<path-to-bfcs-jar>:<path-to-picocli-jar>" <command> org.myorg.MainClass <option> ...
```

Since this gets a little bit unwieldy very quickly, you should set an alias:

```bash
alias bfsc='java -cp <...>'
```

## Sub-commands and Command Line Options

Sub-commands control the action that BFSC is taking. For each type of action there is a sub-command. The way to use this is:

```bash
bfsc <sub-command> <option> ...
```

BFSC recognizes the following sub-commands:

* `cp`: copy files from and to BucketFS

## BucketFS URLs

BFSC uses specific URL to refer to files or paths in a bucket.

```
bfs[s]://<host>:<port>/<bucket>/<path-in-bucket>
```

The protocol is either `bfs` or `bfss` for a connection that is TLS secured.

You specify the host or IP address of the machine where the BucketFS service runs. Each service has its own port that you set in the BucketFS service configuration so a port number identifies the service.

The first path element is always the name of the bucket. The next elements are the path relative to that buckets root.

## Bucket Operations

### General Considerations

#### Password Protected Bucket Access

While buckets can be public for reading depending on their configuration, writing is always password protected. You can provide a password by setting the `--password` (or short `-p`) command line switch.

So for all writing operations on a bucket, the `--password` switch is mandatory.

When you provide the `--password` switch, BFSC will bring up an interactive password prompt with hidden entry after you submitted the command. The reason the password is not specified in on the command line is that this would be a security issue, since the password would then show up in the command history.

As a general rule you should never put any credentials directly in to a command line.

### Copying Files

In the majority of all cases you will copy file _to_ a bucket. For example if you want to install a library that you plan to use in a Python or Java UDF.

```bash
bfsc cp [--password] <from> <to>
```

Example:

```bash
bfsc cp foo-driver-1.2.3.jar bfs://192.168.0.1:2580/default/drivers/foo-driver-1.2.3.jar
```