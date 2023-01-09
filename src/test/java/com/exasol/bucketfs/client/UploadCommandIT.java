package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.Lines.lines;
import static com.exasol.bucketfs.client.IntegrationTestSetup.createLocalFile;
import static com.exasol.bucketfs.client.IntegrationTestSetup.createLocalFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.SOFTWARE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.*;
import org.itsallcode.junit.sysextensions.SystemErrGuard.SysErr;
import org.itsallcode.junit.sysextensions.SystemOutGuard.SysOut;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.ClearSystemProperty;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.profile.ProfileReader;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
@ExtendWith(SystemOutGuard.class)
class UploadCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();
    private static final String FILENAME = "upload.txt";

    @ClearSystemProperty(key = ProfileReader.CONFIG_FILE_PROPERTY)
    @BeforeAll
    static void beforeAll() throws BucketAccessException {
    }

    @AfterAll
    static void after() {
        SETUP.stop();
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    @Test
    void testFailureUploadWithMalformedBucketFsUrlRaisesError(@SysErr final Capturable stream) {
        final BFSC client = BFSC.create("cp", "some_file", "bfs://illegal/");
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> client.run());
        assertThat(stream.getCapturedData(), startsWith("E-BFSC-5: Invalid BucketFS URL: 'bfs://illegal/'"));
    }

    @Test
    void testFailureUploadDirectoryWithoutRecursiveFlag(@TempDir final Path tempDir, @SysErr final Capturable stream)
            throws IOException {
        final BFSC bfsc = BFSC.create("cp", tempDir.toString(), bfsUri(""));
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> bfsc.run());
        assertThat(stream.getCapturedData().trim(), matchesRegex("E-BFSC-8: Cannot upload directory '.*'."
                + " Specify option -r or --recursive to upload directories."));
    }

    @Test
    void testFailureUploadNonExistingFile() throws Exception {
        final String filename = "non-existing-local-file";
        final Path sourceFile = Path.of(filename);
        final BFSC client = BFSC.create("cp", sourceFile.toString(), bfsUri(filename)).feedStdIn(writePassword());
        assertExitWithStatus(SOFTWARE, () -> client.run());
    }

    @Test
    void testUploadDirectoryRecursively(@TempDir final Path tempDir) throws Exception {
        final Path folder = tempDir.resolve("upload");
        Files.createDirectory(folder);
        final List<Path> files = createLocalFiles(folder, "aa.txt", "bb.txt");
        final BFSC client = BFSC.create("cp", "-r", folder.toString(), bfsUri("")).feedStdIn(writePassword());
        assertExitWithStatus(OK, () -> client.run());
        SETUP.waitUntilObjectSynchronized();
        System.out.println(SETUP.listAll(SETUP.getDefaultBucket()));
        for (final Path file : files) {
            verifyFile("upload/" + file.getFileName(), file);
        }
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    // [itest->dsn~sub-command-requires-hidden-password~2]
    @Test
    void testSuccessUpload(@TempDir final Path tempDir) throws Exception {
        final BFSC client = createClient(tempDir, "cp").feedStdIn(writePassword());
        verifyUpload(client, tempDir);
    }

    @Test
    void testUploadWithReadPasswordRequired(@TempDir final Path tempDir, @SysOut final Capturable stream)
            throws Exception {
        final BFSC client = createClient(tempDir, "cp", "-pw") //
                .feedStdIn(SETUP.getDefaultBucket().getReadPassword()) //
                .feedStdIn(writePassword());
        stream.capture();
        verifyUpload(client, tempDir);
        final String stdout = stream.getCapturedData();
        assertThat(stdout, containsString(PasswordReader.prompt("reading from")));
        assertThat(stdout, containsString(PasswordReader.prompt("writing to")));
    }

    @Test
    void testUploadWithBase64EncodedPassword(@TempDir final Path tempDir) throws Exception {
        final BFSC client = createClient(tempDir, "cp", "-d") //
                .feedStdIn(new String(Base64.getEncoder().encode(writePassword().getBytes())));
        verifyUpload(client, tempDir);
    }

    @Test
    void testSuccessUploadWithPassswordFromProfile(@TempDir final Path tempDir) throws Exception {
        final Path configFile = tempDir.resolve(".bucketfs-client-config");
        Files.writeString(configFile, lines("[other-profile]", "password.write=" + writePassword()));
        final BFSC client = createClient(tempDir, "cp", "--profile", "other-profile").withConfigFile(configFile);
        verifyUpload(client, tempDir);
    }

    private void verifyUpload(final BFSC client, final Path tempDir) throws Exception {
        verifyUpload(client, createRandomFile(tempDir), FILENAME);
    }

    @Test
    void testSuccessUploadWithRenaming(@TempDir final Path tempDir) throws Exception {
        final Path local = createRandomFile(tempDir);
        final String remote = "renamed";
        final BFSC client = BFSC.create("cp", local.toString(), bfsUri(remote)).feedStdIn(writePassword());
        verifyUpload(client, local, remote);
    }

    @Test
    void testSuccessUploadToDirectory(@TempDir final Path tempDir) throws Exception {
        final Path local = createRandomFile(tempDir);
        final String remote = "dir/";
        final BFSC client = BFSC.create("cp", local.toString(), bfsUri(remote)).feedStdIn(writePassword());
        verifyUpload(client, local, remote + FILENAME);
    }

    private void verifyUpload(final BFSC client, final Path localFile, final String remotePath)
            throws BucketAccessException, IOException, InterruptedException {
        assertExitWithStatus(OK, () -> client.run());
        SETUP.waitUntilObjectSynchronized();
        verifyFile(remotePath, localFile);
    }

    private void verifyFile(final String remotePath, final Path localFile) throws BucketAccessException, IOException {
        final String actual = SETUP.getDefaultBucket().downloadFileAsString(remotePath);
        assertThat(actual, equalTo(Files.readString(localFile)));
    }

    private Path createRandomFile(final Path tempDir) {
        return createLocalFile(tempDir.resolve(FILENAME), tempDir.getFileName().toString());
    }

    private BFSC createClient(final Path tempDir, final String... initialArgs) {
        final int n = initialArgs.length;
        final String[] args = Arrays.copyOf(initialArgs, n + 2);
        args[n] = tempDir.resolve(FILENAME).toString();
        args[n + 1] = bfsUri(FILENAME);
        return BFSC.create(args);
    }

    private String bfsUri(final String pathInBucket) {
        return SETUP.getDefaultBucketUriToFile(pathInBucket);
    }

    private String writePassword() {
        return SETUP.getDefaultBucket().getWritePassword();
    }
}
