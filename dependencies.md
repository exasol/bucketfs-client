<!-- @formatter:off -->
# Dependencies

## Compile Dependencies

| Dependency                | License                                       |
| ------------------------- | --------------------------------------------- |
| [BucketFS Java][0]        | [MIT License][1]                              |
| [error-reporting-java][2] | [MIT License][3]                              |
| [picocli][4]              | [The Apache Software License, version 2.0][5] |
| [java-ini-parser][6]      | [Apache License, Version 2.0][5]              |
| [Apache Commons IO][7]    | [Apache-2.0][8]                               |
| [Apache Commons Lang][9]  | [Apache-2.0][8]                               |

## Test Dependencies

| Dependency                                      | License                           |
| ----------------------------------------------- | --------------------------------- |
| [JUnit Jupiter (Aggregator)][10]                | [Eclipse Public License v2.0][11] |
| [JUnit Jupiter Params][10]                      | [Eclipse Public License v2.0][11] |
| [Hamcrest][12]                                  | [BSD-3-Clause][13]                |
| [mockito-junit-jupiter][14]                     | [MIT][15]                         |
| [Test containers for Exasol on Docker][16]      | [MIT License][17]                 |
| [Testcontainers :: JUnit Jupiter Extension][18] | [MIT][19]                         |
| [EqualsVerifier \| release normal jar][20]      | [Apache License, Version 2.0][8]  |
| [JUnit5 System Extensions][21]                  | [Eclipse Public License v2.0][22] |
| [junit-pioneer][23]                             | [Eclipse Public License v2.0][11] |
| [SLF4J JDK14 Provider][24]                      | [MIT][25]                         |

## Plugin Dependencies

| Dependency                                              | License                               |
| ------------------------------------------------------- | ------------------------------------- |
| [Apache Maven Clean Plugin][26]                         | [Apache-2.0][8]                       |
| [Apache Maven Install Plugin][27]                       | [Apache-2.0][8]                       |
| [Apache Maven Resources Plugin][28]                     | [Apache-2.0][8]                       |
| [Apache Maven Site Plugin][29]                          | [Apache-2.0][8]                       |
| [SonarQube Scanner for Maven][30]                       | [GNU LGPL 3][31]                      |
| [Apache Maven Toolchains Plugin][32]                    | [Apache-2.0][8]                       |
| [OpenFastTrace Maven Plugin][33]                        | [GNU General Public License v3.0][34] |
| [Project Keeper Maven plugin][35]                       | [The MIT License][36]                 |
| [Apache Maven Compiler Plugin][37]                      | [Apache-2.0][8]                       |
| [Apache Maven Enforcer Plugin][38]                      | [Apache-2.0][8]                       |
| [Maven Flatten Plugin][39]                              | [Apache Software Licenese][8]         |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][40] | [ASL2][5]                             |
| [Maven Surefire Plugin][41]                             | [Apache-2.0][8]                       |
| [Versions Maven Plugin][42]                             | [Apache License, Version 2.0][8]      |
| [duplicate-finder-maven-plugin Maven Mojo][43]          | [Apache License 2.0][44]              |
| [Apache Maven Assembly Plugin][45]                      | [Apache-2.0][8]                       |
| [Apache Maven JAR Plugin][46]                           | [Apache-2.0][8]                       |
| [Artifact reference checker and unifier][47]            | [MIT License][48]                     |
| [Maven Failsafe Plugin][49]                             | [Apache-2.0][8]                       |
| [JaCoCo :: Maven Plugin][50]                            | [EPL-2.0][51]                         |
| [Quality Summarizer Maven Plugin][52]                   | [MIT License][53]                     |
| [error-code-crawler-maven-plugin][54]                   | [MIT License][55]                     |
| [Reproducible Build Maven Plugin][56]                   | [Apache 2.0][5]                       |

[0]: https://github.com/exasol/bucketfs-java/
[1]: https://github.com/exasol/bucketfs-java/blob/main/LICENSE
[2]: https://github.com/exasol/error-reporting-java/
[3]: https://github.com/exasol/error-reporting-java/blob/main/LICENSE
[4]: https://picocli.info
[5]: http://www.apache.org/licenses/LICENSE-2.0.txt
[6]: https://github.com/vincentrussell/java-ini-parser
[7]: https://commons.apache.org/proper/commons-io/
[8]: https://www.apache.org/licenses/LICENSE-2.0.txt
[9]: https://commons.apache.org/proper/commons-lang/
[10]: https://junit.org/junit5/
[11]: https://www.eclipse.org/legal/epl-v20.html
[12]: http://hamcrest.org/JavaHamcrest/
[13]: https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE
[14]: https://github.com/mockito/mockito
[15]: https://opensource.org/licenses/MIT
[16]: https://github.com/exasol/exasol-testcontainers/
[17]: https://github.com/exasol/exasol-testcontainers/blob/main/LICENSE
[18]: https://java.testcontainers.org
[19]: http://opensource.org/licenses/MIT
[20]: https://www.jqno.nl/equalsverifier
[21]: https://github.com/itsallcode/junit5-system-extensions
[22]: http://www.eclipse.org/legal/epl-v20.html
[23]: https://junit-pioneer.org/
[24]: http://www.slf4j.org
[25]: https://opensource.org/license/mit
[26]: https://maven.apache.org/plugins/maven-clean-plugin/
[27]: https://maven.apache.org/plugins/maven-install-plugin/
[28]: https://maven.apache.org/plugins/maven-resources-plugin/
[29]: https://maven.apache.org/plugins/maven-site-plugin/
[30]: http://docs.sonarqube.org/display/PLUG/Plugin+Library/sonar-maven-plugin
[31]: http://www.gnu.org/licenses/lgpl.txt
[32]: https://maven.apache.org/plugins/maven-toolchains-plugin/
[33]: https://github.com/itsallcode/openfasttrace-maven-plugin
[34]: https://www.gnu.org/licenses/gpl-3.0.html
[35]: https://github.com/exasol/project-keeper/
[36]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[37]: https://maven.apache.org/plugins/maven-compiler-plugin/
[38]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[39]: https://www.mojohaus.org/flatten-maven-plugin/
[40]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[41]: https://maven.apache.org/surefire/maven-surefire-plugin/
[42]: https://www.mojohaus.org/versions/versions-maven-plugin/
[43]: https://basepom.github.io/duplicate-finder-maven-plugin
[44]: http://www.apache.org/licenses/LICENSE-2.0.html
[45]: https://maven.apache.org/plugins/maven-assembly-plugin/
[46]: https://maven.apache.org/plugins/maven-jar-plugin/
[47]: https://github.com/exasol/artifact-reference-checker-maven-plugin/
[48]: https://github.com/exasol/artifact-reference-checker-maven-plugin/blob/main/LICENSE
[49]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[50]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[51]: https://www.eclipse.org/legal/epl-2.0/
[52]: https://github.com/exasol/quality-summarizer-maven-plugin/
[53]: https://github.com/exasol/quality-summarizer-maven-plugin/blob/main/LICENSE
[54]: https://github.com/exasol/error-code-crawler-maven-plugin/
[55]: https://github.com/exasol/error-code-crawler-maven-plugin/blob/main/LICENSE
[56]: http://zlika.github.io/reproducible-build-maven-plugin
