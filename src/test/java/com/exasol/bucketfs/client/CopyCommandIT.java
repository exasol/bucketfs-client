package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;
import static com.exasol.bucketfs.Lines.lines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.SOFTWARE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.ExitGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.ClearSystemProperty;

import com.exasol.bucketfs.profile.ProfileReader;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
class CopyCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @ClearSystemProperty(key = ProfileReader.CONFIG_FILE_PROPERTY)
    static void beforeAll() {
    }

    @AfterAll
    static void after() {
        SETUP.stop();
    }

    // [impl->dsn~copy-command-copies-file-from-bucket~1]
    @Test
    void copyFileFromBucketToLocalFile(@TempDir final Path tempDir) throws Exception {
        final String expectedContent = "the content";
        final String filename = "dir_test.txt";
        final Path destinationFile = tempDir.resolve(filename);
        SETUP.uploadStringContent(expectedContent, filename);
        final String source = SETUP.getDefaultBucketUriToFile(filename);
        assertExitWithStatus(OK, () -> BFSC.create("cp", source, destinationFile.toString()).run());
        assertThat(Files.readString(destinationFile), equalTo(expectedContent));
    }

    // [itest->dsn~sub-command-requires-hidden-password~2]
    @Test
    void copyFileFromBucketToFileWithoutProtocol(@TempDir final Path tempDir) throws Exception {
        final String expectedContent = "downloaded content";
        final String filename = "dir_test.txt";
        final Path destinationFile = tempDir.resolve(filename);
        SETUP.uploadStringContent(expectedContent, filename);
        final String source = SETUP.getDefaultBucketUriToFile(filename);
        assertExitWithStatus(OK, () -> BFSC.create("cp", source, destinationFile.toString()).run());
        assertThat(Files.readString(destinationFile), equalTo(expectedContent));
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    // [itest->dsn~sub-command-requires-hidden-password~2]
    @Test
    void copyFileWithoutProtocolToBucket(@TempDir final Path tempDir) throws Exception {
        verifyUpload(tempDir, null, SETUP.getDefaultBucket().getWritePassword());
    }

    @Test
    void copyWithPassswordFromProfile(@TempDir final Path tempDir) throws Exception {
        final Path configFile = tempDir.resolve(".bucketfs-client-config");
        Files.writeString(configFile, lines("[default]", "password=" + SETUP.getDefaultBucket().getWritePassword()));
        verifyUpload(tempDir, configFile, "");
    }

    private void verifyUpload(final Path tempDir, final Path configFile, final String interactivePassword)
            throws Exception {
        final String expectedContent = "uploaded content";
        final String filename = "upload.txt";
        final Path sourceFile = tempDir.resolve(filename);
        Files.writeString(sourceFile, expectedContent);
        final String destination = SETUP.getDefaultBucketUriToFile(filename);
        assertExitWithStatus(OK, () -> BFSC.create("cp", sourceFile.toString(), destination) //
                .feedStdIn(interactivePassword) //
                .withConfigFile(configFile) //
                .run());
        SETUP.waitUntilObjectSynchronized();
        assertThat(SETUP.getDefaultBucket().downloadFileAsString(filename), equalTo(expectedContent));
    }

    @Test
    void copyNonExistingFileWithoutProtocolToBucket() throws Exception {
        final String filename = "non-existing-local-file";
        final Path sourceFile = Path.of(filename);
        final String destination = SETUP.getDefaultBucketUriToFile(filename);
        final String password = SETUP.getDefaultBucket().getWritePassword();
        assertExitWithStatus(SOFTWARE,
                () -> BFSC.create("cp", sourceFile.toString(), destination).feedStdIn(password).run());
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    @Test
    void copyWithMalformedSourceBucketFsUrlRaisesError(final Capturable stream) {
        final BFSC client = BFSC.create("cp", "bfs://illegal/", "some_file");
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> client.run());
        assertThat(stream.getCapturedData(), startsWith("E-BFSC-4: Invalid BucketFS source URL: 'bfs://illegal/'"));
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    @Test
    void copyWithMalformedDestinationBucketFsUrlRaisesError(final Capturable stream) {
        final BFSC client = BFSC.create("cp", "some_file", "bfs://illegal/");
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> client.run());
        assertThat(stream.getCapturedData(),
                startsWith("E-BFSC-3: Invalid BucketFS destination URL: 'bfs://illegal/'"));
    }

    // [itest->dsn~copy-command-copies-file-from-bucket~1]
    @Test
    void downloadingNonexistentObjectRaisesError(final Capturable stream) {
        final String nonexistentObjectUri = SETUP.getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET,
                "/nonexistent-object");
        final BFSC client = BFSC.create("cp", nonexistentObjectUri, "some_file");
        stream.capture();
        assertExitWithStatus(SOFTWARE, () -> client.run());
        assertThat(stream.getCapturedData(), startsWith("E-BFSJ-2: File or directory not found trying to download"));
    }
}
