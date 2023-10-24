# Bucketfs Client 1.1.4, released 2023-10-24

Code name: Dependency Upgrade

## Summary

This release fixes vulnerability CVE-2023-42503 in transitive test dependency to `org.apache.commons:commons-compress` via `exasol-testcontainers` by updating dependencies.

## Security

* #34: Fixed vulnerability CVE-2023-42503 in test dependency `org.apache.commons:commons-compress`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:bucketfs-java:3.1.0` to `3.1.1`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.1` to `6.6.2`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.15.1` to `3.15.2`
* Updated `org.junit-pioneer:junit-pioneer:2.0.1` to `2.1.0`
* Updated `org.mockito:mockito-junit-jupiter:5.5.0` to `5.6.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.7` to `2.0.9`
* Updated `org.testcontainers:junit-jupiter:1.19.0` to `1.19.1`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.3.0` to `1.3.1`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.11` to `2.9.13`
* Removed `org.apache.maven.plugins:maven-enforcer-plugin:3.4.0`
* Updated `org.basepom.maven:duplicate-finder-maven-plugin:1.5.1` to `2.0.1`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.0` to `2.16.1`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.10` to `0.8.11`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184` to `3.10.0.2594`
