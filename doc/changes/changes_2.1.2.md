# Bucketfs Client 2.1.2, released 2024-11-19

Code name: Fix CVE-2024-47554: commons-io:commons-io:jar:2.7:compile

## Summary

This release fixes CVE-2024-47554 in transitive production dependency `commons-io:commons-io:jar:2.7:compile` added by `com.github.vincentrussell:java-ini-parser`.

## Security

* #44: Fixed CVE-2024-47554 in `commons-io:commons-io:jar:2.7:compile`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:bucketfs-java:3.1.2` to `3.2.1`
* Updated `com.github.vincentrussell:java-ini-parser:1.6` to `1.7`
* Added `commons-io:commons-io:2.17.0`
* Added `org.apache.commons:commons-lang3:3.17.0`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.1.0` to `7.1.1`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.16.1` to `3.17.3`
* Updated `org.hamcrest:hamcrest:2.2` to `3.0`
* Updated `org.itsallcode:junit5-system-extensions:1.2.0` to `1.2.2`
* Updated `org.junit-pioneer:junit-pioneer:2.2.0` to `2.3.0`
* Updated `org.junit.jupiter:junit-jupiter-params:5.10.2` to `5.11.3`
* Updated `org.junit.jupiter:junit-jupiter:5.10.2` to `5.11.3`
* Updated `org.mockito:mockito-junit-jupiter:5.11.0` to `5.14.2`
* Updated `org.slf4j:slf4j-jdk14:2.0.13` to `2.0.16`
* Updated `org.testcontainers:junit-jupiter:1.19.7` to `1.20.3`
