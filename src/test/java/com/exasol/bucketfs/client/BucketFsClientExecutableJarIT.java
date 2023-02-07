package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;
import static com.exasol.bucketfs.Lines.lines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.bucketfs.ProcessExecutor;
import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolService;

import picocli.CommandLine.ExitCode;

@Testcontainers
class BucketFsClientExecutableJarIT {
    @Container
    private static ExasolContainer<? extends ExasolContainer<?>> EXASOL = new ExasolContainer<>()//
            .withRequiredServices(ExasolService.BUCKETFS).withReuse(true);

    @Test
    void executableFailsWithoutArguments() throws Exception {
        final ProcessExecutor executor = ProcessExecutor.currentJar().run();
        assertResult(executor, ExitCode.USAGE, equalTo(""), equalTo(lines(//
                "Missing required subcommand", //
                "Usage: bfsc [-hrV] [-pw] [-p=<profileName>] [COMMAND]", //
                "Exasol BucketFS client", //
                "  -h, --help        Show this help message and exit.", //
                "  -p, --profile=<profileName>", //
                "                    name of the profile to use", //
                "      -pw, --require-read-password", //
                "                    whether BFSC should ask for a read password", //
                "  -r, --recursive   recursive", //
                "  -V, --version     Print version information and exit.", //
                "Commands:", //
                "  cp  Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY", //
                "  ls  List contents of PATH", //
                "  rm  Remove file PATH from BucketFS", //
                "")));
    }

    @Test
    void copyFileFailsForWrongPassword(@TempDir final Path tempDir) throws Exception {
        final String filename = "upload.txt";
        final Path sourceFile = tempDir.resolve(filename);
        Files.writeString(sourceFile, "content");
        final String destination = getDefaultBucketUriToFile(filename);
        final ProcessExecutor executor = ProcessExecutor.currentJar() //
                .run("cp", "--profile", "xxx", sourceFile.toString(), destination) //
                .feedStdIn("wrong password");
        assertResult(executor, ExitCode.SOFTWARE, equalTo("Password for writing to BucketFS: "),
                startsWith("E-BFSJ-3: Access denied trying to upload "));
    }

    @Test
    void copyFileFailsForNonExistingFile() throws Exception {
        final Path sourceFile = Path.of("non-existing-file");
        final String destination = getDefaultBucketUriToFile(sourceFile.toString());
        final String password = EXASOL.getClusterConfiguration().getDefaultBucketWritePassword();
        final ProcessExecutor executor = ProcessExecutor.currentJar() //
                .run("cp", "--profile", "xxx", sourceFile.toString(), destination) //
                .feedStdIn(password);
        assertResult(executor, ExitCode.SOFTWARE, equalTo("Password for writing to BucketFS: "),
                equalTo(lines("E-BFSC-2: Unable to upload. No such file or directory: 'non-existing-file'.", "")));
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
        return "bfs://" + EXASOL.getHost() + ":" + EXASOL.getMappedPort(EXASOL.getDefaultInternalBucketfsPort()) + "/"
                + bucketName + "/" + pathInBucket;
    }

    private void assertResult(final ProcessExecutor executor, final int expectedExitCode,
            final Matcher<String> expectedStdOut, final Matcher<String> expectedStdErr)
            throws InterruptedException, IOException {
        executor.assertProcessFinishes();
        assertAll(() -> assertThat(executor.getExitCode(), equalTo(expectedExitCode)), //
                () -> assertThat(executor.getStdOut(), expectedStdOut), //
                () -> assertThat(executor.getStdErr(), expectedStdErr));
    }
}
