# bucketfs-client 0.2.1, released 2022-??-??

Code name: 0.2.1: Make JAR executable

## Summary

The BucketFS client JAR file did not have a main manifest attribute, that's why you had to start it with `java -cp bfsc.jar com.exasol.bucketfs.client.BucketFsClient`. We fixed this, so now you can start it with `java -jar bfsc.jar`.

## Bugfixes

* #8: Added main manifest attribute to make JAR executable

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:bucketfs-java:2.1.0` to `2.3.0`
* Updated `com.exasol:error-reporting-java:0.4.0` to `0.4.1`
* Updated `info.picocli:picocli:4.6.1` to `4.6.3`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.7.1` to `3.10`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:4.0.1` to `6.1.1`
* Updated `org.junit.jupiter:junit-jupiter-engine:5.7.2` to `5.8.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.7.2` to `5.8.2`
* Updated `org.mockito:mockito-junit-jupiter:3.12.4` to `4.5.1`
* Updated `org.testcontainers:junit-jupiter:1.16.0` to `1.17.1`

### Plugin Dependency Updates

* Updated `com.exasol:artifact-reference-checker-maven-plugin:0.3.1` to `0.4.1`
* Updated `com.exasol:error-code-crawler-maven-plugin:0.1.1` to `1.1.1`
* Updated `com.exasol:project-keeper-maven-plugin:1.0.0` to `2.4.2`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.13` to `0.15`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.8.1` to `3.10.1`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:2.8.2` to `2.7`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.0.0-M3` to `3.0.0`
* Removed `org.apache.maven.plugins:maven-gpg-plugin:1.6`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.2.0` to `3.2.2`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.2.0` to `3.4.0`
* Added `org.codehaus.mojo:flatten-maven-plugin:1.2.7`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.7` to `2.10.0`
* Updated `org.itsallcode:openfasttrace-maven-plugin:1.0.0` to `1.5.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.6` to `0.8.8`
* Added `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184`
* Updated `org.sonatype.ossindex.maven:ossindex-maven-plugin:3.1.0` to `3.2.0`
* Removed `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8`
