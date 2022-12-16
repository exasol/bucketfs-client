package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.itsallcode.junit.sysextensions.ExitGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import com.exasol.bucketfs.BucketAccessException;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
// [itest->dsn~list-contents~1]
// [itest->dsn~list-files-and-directories~1]
// [itest->dsn~highlight-type-of-entries~1]
class ListCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @AfterAll
    static void afterAll() {
        SETUP.stop();
    }

    @BeforeAll
    static void beforeAll() throws BucketAccessException {
        SETUP.createFiles("b.txt", "a.txt", "folder/b1.txt", "folder/a1.txt");
    }

    void clean(final boolean cleanAll) throws BucketAccessException {
        if (cleanAll) {
            SETUP.cleanBucketFS();
        } else {
            SETUP.getDefaultBucket().deleteFileNonBlocking("folder/b.txt");
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

    private List<String> listing(final String stdout, final Predicate<String> filter) {
        return Arrays.stream(stdout.split(System.lineSeparator())) //
                .filter(filter) //
                .collect(Collectors.toList());
    }
}
