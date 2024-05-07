# Bucketfs Client 2.1.1, released 2024-05-08

Code name: Improve error message for "connection refused"

## Summary

This release improves the error message when the user tries to connect to a non existing service. Instead of message

```
E-BFSJ-5: I/O error trying to list 'http://localhost:2580/blah'. Cause: null
```

the BucketFS Client now prints the following message:

```
E-BFSJ-5: I/O error trying to list 'http://localhost:2580/blah'. Unable to connect to service, Cause: java.net.ConnectException, Cause: java.net.ConnectException, Cause: java.nio.channels.ClosedChannelException
```

## Bugfixes

* #42: Improve error message for "connection refused"

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:bucketfs-java:3.1.1` to `3.1.2`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.0.1` to `7.1.0`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.15.7` to `3.16.1`
* Updated `org.mockito:mockito-junit-jupiter:5.10.0` to `5.11.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.12` to `2.0.13`
* Updated `org.testcontainers:junit-jupiter:1.19.6` to `1.19.7`
