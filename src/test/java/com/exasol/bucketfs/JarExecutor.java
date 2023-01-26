package com.exasol.bucketfs;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Helps to execute the BucketFS Client's Jar in integration tests or tests for native images.
 */
public class JarExecutor {
    private static final Logger LOGGER = Logger.getLogger(JarExecutor.class.getName());

    private Process process;
    private final Duration timeout = Duration.ofSeconds(5);

    /**
     * Execute jar file in a sub process. Jar must be built before into directory {@code target}.
     *
     * @param args command line arguments for executing the jar file
     * @return this for fluent programming
     * @throws IOException          in case of errors
     * @throws InterruptedException in case process was interrupted
     */
    public JarExecutor run(final String... args) throws IOException, InterruptedException {
        final Path jar = getJarFile();
        final List<String> commandLine = new ArrayList<>(List.of("java", "-jar", jar.toString()));
        commandLine.addAll(asList(args));
        this.process = new ProcessBuilder(commandLine).redirectErrorStream(false).start();
        return this;
    }

    private Path getJarFile() {
        final Path jar = Path.of("target/bfsc-1.1.0.jar").toAbsolutePath();
        if (!Files.exists(jar)) {
            fail("Jar " + jar + " not found. Run 'mvn package' to build it.");
        }
        return jar;
    }

    /**
     * @return content of error standard output stream of process
     */
    public String getStdOut() {
        return readString(this.process.getInputStream());
    }

    /**
     * @return content of standard error stream of process
     */
    public String getStdErr() {
        return readString(this.process.getErrorStream());
    }

    /**
     * @return exit code of process
     */
    public int getExitCode() {
        return this.process.exitValue();
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

    /**
     * Simulate input to stream StdIn.
     *
     * @param value text to pass to stream StdIn
     * @return this for fluent programming
     * @throws IOException in case of an error
     */
    public JarExecutor feedStdIn(final String value) throws IOException {
        LOGGER.fine("Writing value to stdin");
        try (final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(this.process.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(value);
        }
        return this;
    }

    public void assertProcessFinishes() throws InterruptedException {
        LOGGER.fine("Waiting " + this.timeout + " for process to finish...");
        final boolean success = this.process.waitFor(this.timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!success) {
            fail("Process did not finish within timeout of " + this.timeout + ". Std out: '" + getStdOut()
                    + "', std error: '" + getStdErr() + "'");
        }
    }
}
