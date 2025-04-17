package com.exasol.bucketfs.client;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.SystemOutGuard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.Lines;
import com.exasol.bucketfs.url.BucketFsProtocol;

@ExtendWith(SystemOutGuard.class)
// [itest->dsn~list-contents~1]
// [itest->dsn~list-files-and-directories~1]
// [itest->dsn~highlight-type-of-entries~1]
class ListCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @AfterAll
    static void afterAll() {
        SETUP.close();
    }

    @BeforeAll
    static void beforeAll() throws BucketAccessException, InterruptedException {
        SETUP.createRemoteFiles("b.txt", "a.txt", "folder/bb.txt", "folder/aa.txt");
    }

    void clean(final boolean cleanAll) throws BucketAccessException {
        if (cleanAll) {
            SETUP.cleanBucketFS();
        } else {
            SETUP.getDefaultBucket().deleteFileNonBlocking("folder/b.txt");
        }
    }

    @Test
    void testRoot(final Capturable stream) {
        verifyListCommand(stream, createClientWithBucketUri("ls", ""), List.of("a.txt", "b.txt", "folder/"));
    }

    @Test
    void testRecursive(final Capturable stream) {
        verifyListCommand(stream, createClientWithBucketUri("ls", "-r", ""),
                List.of("a.txt", "b.txt", "folder/aa.txt", "folder/bb.txt"));
    }

    @Test
    void testSubdirectory(final Capturable stream) {
        verifyListCommand(stream, createClientWithBucketUri("ls", "folder/"), a -> true, List.of("aa.txt", "bb.txt"));
    }

    @Test
    void testRootWithProfile(@TempDir final Path tempDir, final Capturable stream) throws IOException {
        final BFSC client = createClient("ls").withConfigFile(createConfigFile(tempDir, "default"));
        verifyListCommand(stream, client, List.of("a.txt", "b.txt"));
    }

    @Test
    void testBuckets(@TempDir final Path tempDir, final Capturable stream) throws IOException {
        final BFSC client = createClient("ls").withConfigFile(createConfigFile(tempDir, null));
        verifyListCommand(stream, client, List.of("default"));
    }

    private Path createConfigFile(final Path dir, final String bucket) throws IOException {
        final Path configFile = dir.resolve(".bucketfs-client-config");
        final Optional<Path> tlsCertificatePath = SETUP.getTlsCertificatePath();
        final String protocol = tlsCertificatePath.map(cert -> BucketFsProtocol.BFSS)
                .orElse(BucketFsProtocol.BFS).getName();
        Files.writeString(configFile, Lines.lines("[default]",
                (bucket != null ? "bucket=default" : ""),
                "host=" + SETUP.getHost(),
                "port=" + SETUP.getMappedBucketFsPort(),
                "protocol=" + protocol,
                tlsCertificatePath.map(cert -> "certificate=" + cert.toString()).orElse("")));
        return configFile;
    }

    // There is no test for empty folders as these are not possible in BucketFS.

    @Test
    void nonExistingFolder() {
        final BFSC client = createClientWithBucketUri("ls", "non-existing-folder/");
        client.withExpectedExitCode(1).run();
    }

    private void verifyListCommand(final Capturable stream, final BFSC client, final List<String> expected) {
        final String pattern = expected.stream().collect(Collectors.joining("|"));
        verifyListCommand(stream, client, s -> s.matches(pattern), expected);
    }

    private void verifyListCommand(final Capturable stream, final BFSC client, final Predicate<String> listingFilter,
            final List<String> expected) {
        stream.capture();
        client.run();
        final String stdout = stream.getCapturedData().trim();
        final List<String> actual = listing(stdout, listingFilter);
        assertThat(actual, equalTo(expected));
    }

    private BFSC createClientWithBucketUri(final String... args) {
        final String path = args[args.length - 1];
        args[args.length - 1] = SETUP.getDefaultBucketUriToFile(path);
        return createClient(args).feedStdIn(SETUP.getDefaultBucket().getReadPassword());
    }

    private BFSC createClient(final String... args) {
        final List<String> argsWithCertificate = new ArrayList<>(args.length + 1);
        argsWithCertificate.addAll(asList(args));
        SETUP.getTlsCertificatePath().ifPresent(cert -> argsWithCertificate.add("--certificate=" + cert.toString()));
        return BFSC.create(argsWithCertificate.toArray(new String[0]));
    }

    private List<String> listing(final String stdout, final Predicate<String> filter) {
        final String prompt = PasswordReader.prompt("reading from");
        final String raw = stdout.startsWith(prompt) ? stdout.substring(prompt.length()) : stdout;
        return Arrays.stream(raw.split(System.lineSeparator())) //
                .filter(filter) //
                .collect(Collectors.toList());
    }
}
