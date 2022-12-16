package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.itsallcode.junit.sysextensions.ExitGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.UnsynchronizedBucket;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
class ListCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @AfterAll
    static void afterAll() {
        SETUP.stop();
    }

    @BeforeAll
    static void beforeAll() throws BucketAccessException {
        createFiles();
    }

    void clean(final boolean cleanAll) throws BucketAccessException {
        if (cleanAll) {
            SETUP.cleanBucketFS();
        } else {
            SETUP.getDefaultBucket().deleteFileNonBlocking("folder/b.txt");
        }
    }

    private static void createFile(final UnsynchronizedBucket bucket, final String path) {
        try {
            bucket.uploadStringContentNonBlocking(path, path);
        } catch (BucketAccessException | TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void root() {
        verifyListCommand("", List.of("a.txt", "b.txt", "folder/"));
    }

    @Test
    void subfolder() {
        verifyListCommand("folder/", a -> true, List.of("a1.txt", "b1.txt"));
    }

    // There is no test for empty folders as these are not possible in BucketFS.

    @Test
    void nonExistingFolder() {
        final String path = SETUP.getDefaultBucketUriToFile("non-existing-folder/");
        final BFSC client = BFSC.create("ls", path).catchStdout();
        assertExitWithStatus(1, () -> client.run());
    }

    private static void createFiles() throws BucketAccessException {
        final UnsynchronizedBucket bucket = SETUP.getDefaultBucket();
        Stream.of("b.txt", "a.txt", "folder/b1.txt", "folder/a1.txt") //
                .forEach(path -> createFile(bucket, path));
    }

    private void verifyListCommand(final String folder, final List<String> expected) {
        final String pattern = expected.stream().collect(Collectors.joining("|"));
        verifyListCommand(folder, s -> s.matches(pattern), expected);
    }

    private void verifyListCommand(final String folder, final Predicate<String> listingFilter,
            final List<String> expected) {
        final String path = SETUP.getDefaultBucketUriToFile(folder);
        final BFSC client = BFSC.create("ls", path).catchStdout();
        assertExitWithStatus(OK, () -> client.run());
        final List<String> actual = listing(client.getStdOut(), listingFilter);
        System.out.println(actual);
        assertThat(actual, equalTo(expected));
    }

    private List<String> listing(final String raw, final Predicate<String> filter) {
        return Arrays.stream(raw.split(System.lineSeparator())) //
                .filter(filter) //
                .collect(Collectors.toList());
    }
}
