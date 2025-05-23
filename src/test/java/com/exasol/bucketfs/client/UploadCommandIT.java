package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.Lines.lines;
import static com.exasol.bucketfs.client.IntegrationTestSetup.createLocalFile;
import static com.exasol.bucketfs.client.IntegrationTestSetup.createLocalFiles;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static picocli.CommandLine.ExitCode.SOFTWARE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard.SysErr;
import org.itsallcode.junit.sysextensions.SystemOutGuard;
import org.itsallcode.junit.sysextensions.SystemOutGuard.SysOut;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.profile.ProfileReader;

@ExtendWith(SystemErrGuard.class)
@ExtendWith(SystemOutGuard.class)
class UploadCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();
    private static final String FILENAME = "upload.txt";

    @TempDir
    private Path tempDir;

    @ClearSystemProperty(key = ProfileReader.CONFIG_FILE_PROPERTY)
    @BeforeAll
    static void beforeAll() throws BucketAccessException {
    }

    @AfterAll
    static void after() {
        SETUP.close();
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    @Test
    void testFailureUploadWithMalformedBucketFsUrlRaisesError(@SysErr final Capturable stream) {
        final BFSC client = createClient("cp", "some_file", "bfs://illegal/");
        stream.capture();
        client.withExpectedExitCode(SOFTWARE).run();
        assertThat(stream.getCapturedData(), startsWith("E-BFSC-5: Invalid BucketFS URL: 'bfs://illegal/'"));
    }

    @Test
    void testFailureUploadDirectoryWithoutRecursiveFlag(@SysErr final Capturable stream) throws IOException {
        final BFSC bfsc = createClient("cp", this.tempDir.toString(), bfsUri(""));
        stream.capture();
        bfsc.withExpectedExitCode(SOFTWARE).run();
        assertThat(stream.getCapturedData().trim(), matchesRegex("E-BFSC-8: Cannot upload directory '.*'."
                + " Specify option -r or --recursive to upload directories."));
    }

    @Test
    void testFailureUploadNonExistingFile(@SysErr final Capturable stream) throws Exception {
        final String filename = "non-existing-local-file";
        final Path sourceFile = Path.of(filename);
        stream.capture();
        final BFSC client = createClient("cp", sourceFile.toString(), bfsUri(filename))
                .feedStdIn(writePassword());
        client.withExpectedExitCode(SOFTWARE).run();
        assertThat(stream.getCapturedData().trim(),
                equalTo("E-BFSC-2: Unable to upload. No such file or directory: 'non-existing-local-file'."));
    }

    @Test
    void testUploadDirectoryRecursively() throws Exception {
        final Path folder = this.tempDir.resolve("upload");
        Files.createDirectory(folder);
        final List<Path> files = createLocalFiles(folder, "aa.txt", "bb.txt");
        final BFSC client = createClient("cp", "-r", folder.toString(), bfsUri("")).feedStdIn(writePassword());
        client.run();
        SETUP.waitUntilObjectSynchronized();
        for (final Path file : files) {
            verifyFile("upload/" + file.getFileName(), file);
        }
    }

    // [itest->dsn~copy-command-copies-file-to-bucket~1]
    // [itest->dsn~sub-command-requires-hidden-password~2]
    @Test
    void testSuccessUpload() throws Exception {
        final BFSC client = createClientForUpload("cp").feedStdIn(writePassword());
        verifyUpload(client);
    }

    @Test
    void testUploadWithReadPasswordRequired(@SysOut final Capturable stream) throws Exception {
        final BFSC client = createClientForUpload("cp", "-pw") //
                .feedStdIn(SETUP.getDefaultBucket().getReadPassword()) //
                .feedStdIn(writePassword());
        stream.capture();
        verifyUpload(client);
        final String stdout = stream.getCapturedData();
        assertThat(stdout, containsString(PasswordReader.prompt("reading from")));
        assertThat(stdout, containsString(PasswordReader.prompt("writing to")));
    }

    @Test
    void testSuccessUploadWithPasswordFromProfile() throws Exception {
        final Path configFile = this.tempDir.resolve(".bucketfs-client-config");
        Files.writeString(configFile, lines("[other-profile]", "password.write=" + writePassword()));
        final BFSC client = createClientForUpload("cp", "--profile", "other-profile").withConfigFile(configFile);
        verifyUpload(client);
    }

    private void verifyUpload(final BFSC client) throws Exception {
        verifyUpload(client, createRandomFile(), FILENAME);
    }

    @Test
    void testSuccessUploadWithRenaming() throws Exception {
        final Path local = createRandomFile();
        final String remote = "renamed";
        final BFSC client = createClient("cp", local.toString(), bfsUri(remote)).feedStdIn(writePassword());
        verifyUpload(client, local, remote);
    }

    @Test
    void testSuccessUploadToDirectory() throws Exception {
        final Path local = createRandomFile();
        final String remote = "dir/";
        final BFSC client = createClient("cp", local.toString(), bfsUri(remote)).feedStdIn(writePassword());
        verifyUpload(client, local, remote + FILENAME);
    }

    @ParameterizedTest
    @CsvSource({ "some_file", "./some_file" })
    void testSuccessUploadFile(final String localPath) throws Exception {
        final String remote = "uploaded_file";
        final BFSC client = createClient("cp", localPath, bfsUri(remote)).feedStdIn(writePassword());
        verifyUpload(client, Path.of("some_file"), remote);
    }

    private void verifyUpload(final BFSC client, final Path localFile, final String remotePath)
            throws BucketAccessException, IOException, InterruptedException {
        client.run();
        SETUP.waitUntilObjectSynchronized();
        verifyFile(remotePath, localFile);
    }

    private void verifyFile(final String remotePath, final Path localFile) throws BucketAccessException, IOException {
        final String actual = SETUP.getDefaultBucket().downloadFileAsString(remotePath);
        assertThat(actual, equalTo(Files.readString(localFile)));
    }

    private Path createRandomFile() {
        return createLocalFile(this.tempDir.resolve(FILENAME), String.valueOf(System.currentTimeMillis()));
    }

    private BFSC createClient(final String... args) {
        final List<String> argsWithCertificate = new ArrayList<>(args.length + 1);
        argsWithCertificate.addAll(asList(args));
        // [itest -> dsn~tls-support.self-signed-certificates~1]
        SETUP.getTlsCertificatePath().ifPresent(cert -> argsWithCertificate.add("--certificate=" + cert.toString()));
        return BFSC.create(argsWithCertificate.toArray(new String[0]));
    }

    private BFSC createClientForUpload(final String... initialArgs) {
        final int n = initialArgs.length;
        final String[] args = Arrays.copyOf(initialArgs, n + 2);
        args[n] = this.tempDir.resolve(FILENAME).toString();
        args[n + 1] = bfsUri(FILENAME);
        return createClient(args);
    }

    private String bfsUri(final String pathInBucket) {
        return SETUP.getDefaultBucketUriToFile(pathInBucket);
    }

    private String writePassword() {
        return SETUP.getDefaultBucket().getWritePassword();
    }
}
