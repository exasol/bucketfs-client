# Bucketfs Client 2.2.0, released 2025-04-22

Code name: TLS support

## Summary

This release adds support for connecting to BucketFS via TLS. Command line option `--certificate` allows specifying the server certificate when it is self-signed. See the user guide for details.

## Features

* #33: Added TLS support

## Dependency Updates

### Compile Dependency Updates

* Updated `commons-io:commons-io:2.17.0` to `2.19.0`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.1.1` to `7.1.4`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.17.3` to `3.19.3`
* Added `org.junit.jupiter:junit-jupiter-api:5.12.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.11.3` to `5.12.2`
* Removed `org.junit.jupiter:junit-jupiter:5.11.3`
* Updated `org.mockito:mockito-junit-jupiter:5.14.2` to `5.17.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.16` to `2.0.17`
* Updated `org.testcontainers:junit-jupiter:1.20.3` to `1.20.6`

### Plugin Dependency Updates

* Updated `com.exasol:artifact-reference-checker-maven-plugin:0.4.2` to `0.4.3`
* Updated `com.exasol:error-code-crawler-maven-plugin:2.0.0` to `2.0.3`
* Updated `com.exasol:project-keeper-maven-plugin:4.1.0` to `5.0.1`
* Added `com.exasol:quality-summarizer-maven-plugin:0.2.0`
* Added `io.github.git-commit-id:git-commit-id-maven-plugin:9.0.1`
* Removed `io.github.zlika:reproducible-build-maven-plugin:0.16`
* Added `org.apache.maven.plugins:maven-artifact-plugin:3.6.0`
* Updated `org.apache.maven.plugins:maven-assembly-plugin:3.6.0` to `3.7.1`
* Updated `org.apache.maven.plugins:maven-clean-plugin:3.2.0` to `3.4.1`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.12.1` to `3.14.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.4.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.2.5` to `3.5.2`
* Updated `org.apache.maven.plugins:maven-install-plugin:3.1.2` to `3.1.4`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.3.0` to `3.4.2`
* Updated `org.apache.maven.plugins:maven-site-plugin:3.12.1` to `3.21.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.2.5` to `3.5.2`
* Updated `org.apache.maven.plugins:maven-toolchains-plugin:3.1.0` to `3.2.0`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.6.0` to `1.7.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.2` to `2.18.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.11` to `0.8.12`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.10.0.2594` to `5.0.0.4389`
