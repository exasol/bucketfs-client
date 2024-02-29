# Bucketfs Client 1.1.5, released 2024-02-29

Code name: Fix CVE-2024-25710 and CVE-2024-26308 in test dependency `org.apache.commons:commons-compress`

## Summary

This release fixes vulnerabilities CVE-2024-25710 and CVE-2024-26308 in test dependency `org.apache.commons:commons-compress`.

### Breaking Change

Starting with this release running the `.jar` file requires Java 17 or later. See the [user guide](https://github.com/exasol/bucketfs-client/blob/main/doc/user_guide/user_guide.md#running-the-jar-file) for details.

## Security

* #36: Fixed CVE-2024-25710 and CVE-2024-26308 in test dependency `org.apache.commons:commons-compress`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.github.vincentrussell:java-ini-parser:1.5` to `1.6`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.2` to `7.0.1`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.15.2` to `3.15.7`
* Updated `org.junit-pioneer:junit-pioneer:2.1.0` to `2.2.0`
* Updated `org.junit.jupiter:junit-jupiter-params:5.10.0` to `5.10.2`
* Updated `org.junit.jupiter:junit-jupiter:5.10.0` to `5.10.2`
* Updated `org.mockito:mockito-junit-jupiter:5.6.0` to `5.10.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.9` to `2.0.12`
* Updated `org.testcontainers:junit-jupiter:1.19.1` to `1.19.6`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.3.1` to `2.0.0`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.14` to `4.1.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.11.0` to `3.12.1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.1.2` to `3.2.5`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.1.2` to `3.2.5`
* Added `org.apache.maven.plugins:maven-toolchains-plugin:3.1.0`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.5.0` to `1.6.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.1` to `2.16.2`
