package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;
import static com.exasol.bucketfs.Lines.lines;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.exasol.bucketfs.ProcessExecutor;

import picocli.CommandLine.ExitCode;

class BucketFsClientExecutableJarIT {
    // BucketFS PUT requests will block until the UDF container is unpacked, causing timeouts. So we need to wait for
    // UDF, too.
    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @TempDir
    Path tempDir;

    @Test
    void executableFailsWithoutArguments() throws Exception {
        final ProcessExecutor executor = ProcessExecutor.currentJar().run();
        assertResult(executor, ExitCode.USAGE, equalTo(""), equalTo(
                """
                        Missing required subcommand
                        Usage: bfsc [-hrV] [-pw] [-c=<tlsCertificatePath>] [-p=<profileName>] [COMMAND]
                        Exasol BucketFS client
                          -c, --certificate=<tlsCertificatePath>
                                            local path to the server's TLS certificate in case the
                                              certificate is not contained in the Java keystore
                          -h, --help        Show this help message and exit.
                          -p, --profile=<profileName>
                                            name of the profile to use
                              -pw, --require-read-password
                                            whether BFSC should ask for a read password
                          -r, --recursive   recursive
                          -V, --version     Print version information and exit.
                        Commands:
                          cp  Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY
                          ls  List contents of PATH
                          rm  Remove file PATH from BucketFS
                        """));
    }

    @Test
    void copyFileFailsForWrongPassword(@TempDir final Path tempDir) throws Exception {
        final String filename = "upload.txt";
        final Path sourceFile = tempDir.resolve(filename);
        Files.writeString(sourceFile, "content");
        final String destination = getDefaultBucketUriToFile(filename);
        final List<String> args = new ArrayList<>();
        args.addAll(asList("cp", "--profile", "xxx", sourceFile.toString(), destination));
        SETUP.getTlsCertificatePath().ifPresent(cert -> {
            args.add("--certificate");
            args.add(cert.toString());
        });
        final ProcessExecutor executor = ProcessExecutor.currentJar()
                .run(args)
                .feedStdIn("wrong password");
        assertResult(executor, ExitCode.SOFTWARE, equalTo("Password for writing to BucketFS: "),
                containsString("E-BFSJ-3: Access denied trying to upload "));
    }

    @Test
    void copyFileFailsForNonExistingFile() throws Exception {
        final Path sourceFile = Path.of("non-existing-file");
        final String destination = getDefaultBucketUriToFile(sourceFile.toString());
        final String password = SETUP.getClusterConfiguration().getDefaultBucketWritePassword();
        final ProcessExecutor executor = ProcessExecutor.currentJar() //
                .run("cp", "--profile", "xxx", sourceFile.toString(), destination) //
                .feedStdIn(password);
        assertResult(executor, ExitCode.SOFTWARE, equalTo("Password for writing to BucketFS: "),
                equalTo("""
                        E-BFSC-2: Unable to upload. No such file or directory: 'non-existing-file'.
                        """));
    }

    @Test
    void copyFileWithoutDirectory() throws Exception {
        final Path tempFile = tempDir.resolve("test-file.txt");
        final String fileContent = "test file content";
        Files.writeString(tempFile, fileContent);

        final String fileName = tempFile.getFileName().toString();
        final String destination = getDefaultBucketUriToFile(fileName);
        final String password = SETUP.getClusterConfiguration().getDefaultBucketWritePassword();
        final List<String> args = new ArrayList<>();
        args.addAll(asList("cp", "--profile", "xxx", fileName, destination));
        SETUP.getTlsCertificatePath().ifPresent(cert -> {
            args.add("--certificate");
            args.add(cert.toString());
        });
        final ProcessExecutor executor = ProcessExecutor.currentJar().workingDirectory(tempDir)
                .run(args)
                .feedStdIn(password);
        assertResult(executor, ExitCode.OK, equalTo("Password for writing to BucketFS: "),
                anyOf(containsString("INFO: Allow additional subject alternative names (SAN)"), emptyString()));
        assertThat(SETUP.getDefaultBucket().downloadFileAsString(fileName), equalTo(fileContent));
    }

    @Test
    void listFailsWhenServerNotListening() throws Exception {
        final String url = "bfs://localhost:65535/bucket/";
        final ProcessExecutor executor = ProcessExecutor.currentJar().run("ls", "-pw", url) //
                .feedStdIn("somePassword");
        assertResult(executor, ExitCode.SOFTWARE, equalTo("Password for reading from BucketFS: "), equalTo(
                "E-BFSJ-5: I/O error trying to list 'http://localhost:65535/bucket'. Unable to connect to service, Cause: java.net.ConnectException, Cause: java.net.ConnectException, Cause: java.nio.channels.ClosedChannelException\n"));
    }

    @Test
    void testVersion() throws Exception {
        final ProcessExecutor executor = ProcessExecutor.currentJar().run("--version");
        assertResult(executor, ExitCode.OK, equalTo(lines(executor.getJarVersion(), "")), equalTo(""));
    }

    private String getDefaultBucketUriToFile(final String filename) {
        return getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
    }

    private String getBucketFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return SETUP.getBucketFsUri(serviceName, bucketName, pathInBucket);
    }

    private void assertResult(final ProcessExecutor executor, final int expectedExitCode,
            final Matcher<String> expectedStdOut, final Matcher<String> expectedStdErr)
            throws InterruptedException, IOException {
        executor.assertProcessFinishes();
        assertAll(() -> assertThat("exit code", executor.getExitCode(), equalTo(expectedExitCode)),
                () -> assertThat("std out", executor.getStdOut(), expectedStdOut),
                () -> assertThat("std err", executor.getStdErr(), expectedStdErr));
    }
}
