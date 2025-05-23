# BucketFS Client 2.2.1, released 2025-05-13

Code name: Documentation improvements on top of 2.2.0

## Summary

We improved the content, language and understandability of the BucketFS client user guide.

Please note that we are in the process of removing some URL auto-magic that uses profile information, since that turned out to be confusing rather than convenient.

We recommend using full BFS URLs, since the shortening will be removed with [#54](https://github.com/exasol/bucketfs-client/issues/54).

The profile configuration as such will remain available, since it is required to hide passwords and point to server certificates.

## Features

* 38: Improved user documentation

## Dependency Updates

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:5.0.1` to `5.1.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.5.2` to `3.5.3`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.5.2` to `3.5.3`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.12` to `0.8.13`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389` to `5.1.0.4751`
