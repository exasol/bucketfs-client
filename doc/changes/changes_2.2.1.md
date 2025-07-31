# BucketFS Client 2.2.1, released 2025-08-01

Code name: Documentation improvements on top of 2.2.0, fix of CVE-2025-48924

## Summary

We improved the content, language and understandability of the BucketFS client user guide.

Please note that we are in the process of removing some URL auto-magic that uses profile information, since that turned out to be confusing rather than convenient.

We recommend using full BFS URLs, since the shortening will be removed with [#54](https://github.com/exasol/bucketfs-client/issues/54).

The profile configuration as such will remain available, since it is required to hide passwords and point to server certificates.

We also update transitive dependency to fix CVE-2025-48924.

## Features

* 38: Improved user documentation

## Security

* #56: CVE-2025-48924: org.apache.commons:commons-lang3:jar:3.17.0:compile

## Dependency Updates

### Compile Dependency Updates

* Updated `org.apache.commons:commons-lang3:3.17.0` to `3.18.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:2.0.3` to `2.0.4`
* Updated `com.exasol:project-keeper-maven-plugin:5.0.1` to `5.2.3`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.5.2` to `3.5.3`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.5.2` to `3.5.3`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.12` to `0.8.13`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389` to `5.1.0.4751`
