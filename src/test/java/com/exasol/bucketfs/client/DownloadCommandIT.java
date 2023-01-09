package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.client.IntegrationTestSetup.content;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.SOFTWARE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.ExitGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.exasol.bucketfs.BucketAccessException;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
class DownloadCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @BeforeAll
    static void beforeAll() throws BucketAccessException, InterruptedException {
        // parent needs to be created *after* child, otherwise HTTP client method send() blocks.
        SETUP.createRemoteFiles("a.txt", "folder/aa.txt", "problem/ambigue/any.txt", "problem/ambigue");
    }

    @AfterAll
    static void after() {
        SETUP.stop();
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    @Test
    void testFailureDownloadWithMalformedBucketFsUrl(final Capturable stream) {
        final BFSC client = BFSC.create("cp", "bfs://illegal/", "some_file");
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> client.run());
        assertThat(stream.getCapturedData(), startsWith("E-BFSC-5: Invalid BucketFS URL: 'bfs://illegal/'"));
    }

    // [itest->dsn~copy-command-copies-file-from-bucket~1]
    @Test
    void testFailureDownloadNonExistingObject(final Capturable stream) {
        verifyNoSuchFile("non-existing-object", stream);
    }

    @Test
    void testFailureDownloadRoot(final Capturable stream) {
        verifyNoSuchFile("", stream);
    }

    private void verifyNoSuchFile(final String remote, final Capturable stream) {
        final BFSC client = BFSC.create("cp", bfsUri(remote), "some_file");
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> client.run());
        assertThat(stream.getCapturedData().trim(),
                matchesRegex("E-BFSC-10: Cannot download '.*/" + remote + "': No such file or directory."));
    }

    private String bfsUri(final String pathInBucket) {
        return SETUP.getDefaultBucketUriToFile(pathInBucket);
    }

    @Test
    void testFailureDownloadDirectoryWithoutRecursiveFlag(@TempDir final Path tempDir, final Capturable stream) {
        final String remote = "folder/";
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> BFSC.create("cp", bfsUri(remote), tempDir.toString()).run());
        assertThat(stream.getCapturedData().trim(), matchesRegex("E-BFSC-12: Cannot download directory '.*/" + remote
                + "'." + " Specify option -r or --recursive to download directories."));
    }

    @Test
    void testFailureDownloadDirectoryToFile(@TempDir final Path tempDir, final Capturable stream) throws IOException {
        final String remote = "folder/";
        final Path local = Files.createFile(tempDir.resolve("existing.txt"));
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> BFSC.create("cp", "-r", bfsUri(remote), local.toString()).run());
        assertThat(stream.getCapturedData().trim(), matchesRegex("E-BFSC-13: Cannot overwrite local non-directory '.*"
                + local.getFileName() + "' with download from directory '.*/" + remote + "'."));
    }

    // [itest->dsn~download-ambigue-entry-recursively~1]
    @Test
    void testFailureDownloadAmbigueRecursively(@TempDir final Path tempDir, final Capturable stream) {
        final String remote = "problem/ambigue";
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> BFSC.create("cp", "-r", bfsUri(remote), tempDir.toString()).run());
        assertThat(stream.getCapturedData().trim(),
                matchesRegex("E-BFSC-11: Cannot download regular file and directory with identical name." //
                        + " Known mitigations:\n" //
                        + "\\* Append trailing path separator '/' to '.*/" + remote
                        + "' to download directory recursively.\n" //
                        + "\\* Remove option -r or --recursive to download the regular file."));
    }

    // [itest->dsn~copy-command-copies-file-from-bucket~1]
    @Test
    void testSuccessDownloadWithoutProtocol(@TempDir final Path tempDir) throws Exception {
        final String remote = "a.txt";
        final Path local = tempDir.resolve(remote);
        assertExitWithStatus(OK, () -> BFSC.create("cp", bfsUri(remote), local.toString()).run());
        assertThat(Files.readString(local), equalTo(content(remote)));
    }

    // [itest->dsn~copy-ambigue-entrie-on-lower-level~1]
    @Test
    void testWarningDownloadSkipsAmbigueChildren(@TempDir final Path tempDir, final Capturable stream)
            throws IOException {
        stream.capture();
        assertExitWithStatus(OK, () -> BFSC.create("cp", "-r", bfsUri("problem"), tempDir.toString()).run());
        assertThat(stream.getCapturedData().trim(), matchesRegex("W-BFSC-14: " //
                + "Skipping ambigue file '.*/problem/ambigue'" //
                + " as BucketFS contains directory with identical name."));
        final String remote = "problem/ambigue/any.txt";
        assertThat(Files.readString(tempDir.resolve(remote)), equalTo(content(remote)));
    }

    // [itest->dsn~download-ambigue-file~1]
    @Test
    void testSuccessDownloadAmbigueFile(@TempDir final Path tempDir) throws IOException {
        final String remote = "problem/ambigue";
        final Path local = tempDir.resolve("new.txt");
        assertExitWithStatus(OK, () -> BFSC.create("cp", bfsUri(remote), local.toString()).run());
        assertThat(Files.readString(local), equalTo(content(remote)));
    }

    // [itest->dsn~download-ambigue-directory-recursively~1]
    @Test
    void testSuccessDownloadAmbigueDirectory(@TempDir final Path tempDir) throws IOException {
        final String remote = "problem/ambigue/";
        assertExitWithStatus(OK, () -> BFSC.create("cp", "-r", bfsUri(remote), tempDir.toString()).run());
        assertThat(Files.readString(tempDir.resolve("ambigue/any.txt")), equalTo(content("problem/ambigue/any.txt")));
    }

    @Test
    void testSuccessDownloadDirectoryIntoExistingDirectory(@TempDir final Path tempDir) throws IOException {
        final String remote = "folder/";
        final Path local = Files.createDirectory(tempDir.resolve("local-folder"));
        assertExitWithStatus(OK, () -> BFSC.create("cp", "-r", bfsUri(remote), local.toString()).run());
        final Path download = local.resolve(remote);
        final String filename = "aa.txt";
        assertThat(Files.readString(download.resolve(filename)), equalTo(content(remote + filename)));
    }

    @Test
    void testSuccessDownloadDirectoryWithRenaming(@TempDir final Path tempDir) throws IOException {
        final String remote = "folder/";
        final Path download = tempDir.resolve("local-folder");
        assertExitWithStatus(OK, () -> BFSC.create("cp", "-r", bfsUri(remote), download.toString()).run());
        final String filename = "aa.txt";
        assertThat(Files.readString(download.resolve(filename)), equalTo(content(remote + filename)));
    }
}
