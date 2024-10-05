# Bucketfs Client 2.1.2, released 2024-??-??

Code name: Fixed vulnerability CVE-2024-47554 in commons-io:commons-io:jar:2.7:compile

## Summary

This release fixes the following vulnerability:

### CVE-2024-47554 (CWE-400) in dependency `commons-io:commons-io:jar:2.7:compile`
Uncontrolled Resource Consumption vulnerability in Apache Commons IO.

The org.apache.commons.io.input.XmlStreamReader class may excessively consume CPU resources when processing maliciously crafted input.

This issue affects Apache Commons IO: from 2.0 before 2.14.0.

Users are recommended to upgrade to version 2.14.0 or later, which fixes the issue.
#### References
* https://ossindex.sonatype.org/vulnerability/CVE-2024-47554?component-type=maven&component-name=commons-io%2Fcommons-io&utm_source=ossindex-client&utm_medium=integration&utm_content=1.8.1
* http://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2024-47554
* https://lists.apache.org/thread/6ozr91rr9cj5lm0zyhv30bsp317hk5z1

## Security

* #44: Fixed vulnerability CVE-2024-47554 in dependency `commons-io:commons-io:jar:2.7:compile`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:bucketfs-java:3.1.2` to `3.2.0`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.1.0` to `7.1.1`
* Updated `nl.jqno.equalsverifier:equalsverifier:3.16.1` to `3.17.1`
* Updated `org.hamcrest:hamcrest:2.2` to `3.0`
* Updated `org.itsallcode:junit5-system-extensions:1.2.0` to `1.2.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.10.2` to `5.11.2`
* Updated `org.junit.jupiter:junit-jupiter:5.10.2` to `5.11.2`
* Updated `org.mockito:mockito-junit-jupiter:5.11.0` to `5.14.1`
* Updated `org.slf4j:slf4j-jdk14:2.0.13` to `2.0.16`
* Updated `org.testcontainers:junit-jupiter:1.19.7` to `1.20.2`
