package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;
import static com.exasol.bucketfs.Lines.lines;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolService;

import picocli.CommandLine.ExitCode;

@Testcontainers
class BucketFsClientExecutableJarIT {
    private static final Logger LOGGER = Logger.getLogger(BucketFsClientExecutableJarIT.class.getName());
    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(5);
    @Container
    private static ExasolContainer<? extends ExasolContainer<?>> EXASOL = new ExasolContainer<>()//
            .withRequiredServices(ExasolService.BUCKETFS).withReuse(true);

    @Test
    void executableFailsWithoutArguments() throws Exception {
        assertProcessFails(run(), ExitCode.USAGE, equalTo(""), equalTo(lines(//
                "Missing required subcommand", //
                "Usage: bfsc [-r] [-pw] [-p=<profileName>] [COMMAND]", //
                "Exasol BucketFS client", //
                "  -p, --profile=<profileName>", //
                "                    name of the profile to use", //
                "      -pw, --require-read-password", //
                "                    whether BFSC should ask for a read password", //
                "  -r, --recursive   recursive", //
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
        final Process process = run("cp", sourceFile.toString(), destination);
        writeToStdIn(process, "wrong password");
        assertProcessFails(process, ExitCode.SOFTWARE, equalTo(PasswordReader.prompt("writing to")),
                startsWith("E-BFSJ-3: Access denied trying to upload "));
    }

    @Test
    void copyFileFailsForNonExistingFile() throws Exception {
        final Path sourceFile = Path.of("non-existing-file");
        final String destination = getDefaultBucketUriToFile(sourceFile.toString());
        final String password = EXASOL.getClusterConfiguration().getDefaultBucketWritePassword();
        final Process process = run("cp", sourceFile.toString(), destination);
        writeToStdIn(process, password);
        assertProcessFails(process, ExitCode.SOFTWARE, equalTo(PasswordReader.prompt("writing to")),
                equalTo(lines("E-BFSC-2: Unable to upload. No such file or directory: 'non-existing-file'.", "")));
    }

    private void writeToStdIn(final Process process, final String value) throws IOException {
        LOGGER.fine("Writing value to stdin");
        try (final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(value);
        }
    }

    private String getDefaultBucketUriToFile(final String filename) {
        return getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
    }

    private String getBucketFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return "bfs://" + EXASOL.getHost() + ":" + EXASOL.getMappedPort(EXASOL.getDefaultInternalBucketfsPort()) + "/"
                + bucketName + "/" + pathInBucket;
    }

    private Process run(final String... args) throws IOException, InterruptedException {
        final Path jar = getJarFile();
        final List<String> commandLine = new ArrayList<>(List.of("java", "-jar", jar.toString()));
        commandLine.addAll(asList(args));
        LOGGER.info("Launching command '" + String.join(" ", commandLine) + "'...");
        final Process process = new ProcessBuilder(commandLine).redirectErrorStream(false).start();
        return process;
    }

    private Path getJarFile() {
        final Path jar = Path.of("target/bfsc-1.0.0.jar").toAbsolutePath();
        if (!Files.exists(jar)) {
            fail("Jar " + jar + " not found. Run 'mvn package' to build it.");
        }
        return jar;
    }

    private void assertProcessFinishes(final Process process, final Duration timeout) throws InterruptedException {
        LOGGER.fine("Waiting " + timeout + " for process to finish...");
        final boolean success = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!success) {
            final String stdOut = readString(process.getInputStream());
            final String stdErr = readString(process.getErrorStream());
            fail("Process did not finish within timeout of " + timeout + ". Std out: '" + stdOut + "', std error: '"
                    + stdErr + "'");
        }
    }

    private void assertProcessFails(final Process process, final int expectedExitCode,
            final Matcher<String> expectedStdOut, final Matcher<String> expectedStdErr)
            throws InterruptedException, IOException {
        assertProcessFinishes(process, PROCESS_TIMEOUT);
        final int exitCode = process.exitValue();
        final String stdOut = readString(process.getInputStream());
        final String stdErr = readString(process.getErrorStream());
        assertAll(() -> assertThat(exitCode, equalTo(expectedExitCode)), //
                () -> assertThat(stdOut, expectedStdOut), //
                () -> assertThat(stdErr, expectedStdErr));
    }

    private String readString(final InputStream stream) {
        try {
            if (stream.available() < 1) {
                return "";
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
