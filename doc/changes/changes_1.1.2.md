# Bucketfs Client 1.1.2, released 2023-03-20

Code name: Updated Documentation

## Summary

As some platforms warn when downloading or even refuse to execute unsigned binaries this release removes the native binary for macOS and adds information to the user guide.

## Release Contents and Documentation

* #26: Removed build for macOS and updated documentation
* #25: Documented fall back to running the jar file for platforms not supported currently
* #27: Added information on browser warnings when downloading unsigned binaries on Windows platform

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:1.0.0` to `1.0.1`
* Updated `com.github.vincentrussell:java-ini-parser:1.4` to `1.5`
* Updated `info.picocli:picocli:4.7.0` to `4.7.1`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.5.0` to `6.5.1`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.12.3` to `3.14.1`
* Updated `org.junit-pioneer:junit-pioneer:1.9.1` to `2.0.0`
* Updated `org.mockito:mockito-junit-jupiter:5.0.0` to `5.2.0`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.3` to `2.9.4`
* Updated `org.apache.maven.plugins:maven-assembly-plugin:3.4.2` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.1.0` to `3.2.1`
