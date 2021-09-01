# bucketfs-client 0.2.0, released 2021-09-01

Code name: Documentation improvements

## Summary

BucketFS client 0.2.0 contains a *breaking change* in the Bucket URL scheme. We removed the BucketFS service name since it was redundant with the port number. While the service name is easier to remember than a port number, this also led to confusion and potential conflict between port number and service name.

Bucket FS URLs now have the following format:

```
bfs[s]://<host>:<port>/<bucket>/<path-in-bucket>
```

We also updated dependencies and documented the prerequisites in the user guide.

## Bugfix

* #5: Remove redundant BucketFS service name from URL

## Refactoring

* #4: Switched from Travis CI to GitHub actions
* #4: Updated dependencies

## Documentation

* #3: Added missing dependencies file
* #4: Added prerequisites in user guide


## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:bucketfs-java:2.0.1` to `2.1.0`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.6.1` to `3.7.1`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:3.5.3` to `4.0.1`
* Updated `org.mockito:mockito-junit-jupiter:3.11.2` to `3.12.4`
* Updated `org.testcontainers:junit-jupiter:1.15.3` to `1.16.0`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:0.6.1` to `1.0.0`
* Updated `org.apache.maven.plugins:maven-jar-plugin:2.4` to `3.2.0`
