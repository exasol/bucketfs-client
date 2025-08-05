package com.exasol.bucketfs;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Helps to execute the BucketFS Client's Jar in integration tests or to run native images.
 */
public class ProcessExecutor {

    private static final Logger LOGGER = Logger.getLogger(ProcessExecutor.class.getName());
    private static final Duration TIMEOUT = Duration.ofSeconds(100);

    /** Name of the JAR file */
    public static final String JAR_NAME = "bfsc-2.2.1.jar";

    /**
     * Create a {@link ProcessExecutor} for the jar built by for the current projects. The Jar file must be built before
     * into directory {@code target}.
     *
     * @return new instance of {@link ProcessExecutor}
     */
    public static ProcessExecutor currentJar() {
        return new ProcessExecutor("java", "-jar", getJarFile().toString());
    }

    private static Path getJarFile() {
        final Path jar = Path.of("target").resolve(JAR_NAME).toAbsolutePath();
        if (!Files.exists(jar)) {
            fail("Jar " + jar + " not found. Run 'mvn package' to build it.");
        }
        return jar;
    }

    private final String[] initialArgs;
    private Process process;
    private Path workingDir = null;

    /**
     * Create a new instance of {@link ProcessExecutor}.
     *
     * @param initialArgs initial arguments to run the current executable
     */
    public ProcessExecutor(final String... initialArgs) {
        this.initialArgs = initialArgs;
    }

    /**
     * Set the working directory for the new process.
     * <p>
     * Default value {@code null} is the current working directory.
     * 
     * @param workingDir the working directory for the new process
     * @return this instance for method chaining
     */
    public ProcessExecutor workingDirectory(final Path workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    /**
     * Execute sub process defined by this {@link ProcessExecutor}.
     *
     * @param args additional command line arguments for execution
     * @return this for fluent programming
     * @throws IOException in case of errors
     */
    public ProcessExecutor run(final List<String> args) throws IOException {
        return run(args.toArray(new String[0]));
    }

    /**
     * Execute sub process defined by this {@link ProcessExecutor}.
     *
     * @param args additional command line arguments for execution
     * @return this for fluent programming
     * @throws IOException in case of errors
     */
    public ProcessExecutor run(final String... args) throws IOException {
        final List<String> commandLine = new ArrayList<>(asList(this.initialArgs));
        commandLine.addAll(asList(args));
        final File directory = Optional.ofNullable(workingDir).map(Path::toAbsolutePath).map(Path::toFile).orElse(null);
        this.process = new ProcessBuilder(commandLine).redirectErrorStream(false).directory(directory).start();
        LOGGER.fine(() -> "Running command " + commandLine + " in directory " + directory + " with PID " + process.pid()
                + "...");
        return this;
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
    public ProcessExecutor feedStdIn(final String value) throws IOException {
        LOGGER.fine("Writing value to stdin");
        try (final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(this.process.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(value);
        }
        return this;
    }

    public void assertProcessFinishes() throws InterruptedException {
        LOGGER.fine("Waiting " + TIMEOUT + " for process " + this.process.pid() + " to finish...");
        final boolean success = this.process.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        if (!success) {
            final String message = "Process " + process.pid() + " did not finish within timeout of " + TIMEOUT
                    + ". Std out: '" + getStdOut() + "', std error: '" + getStdErr() + "'";
            LOGGER.warning(message);
            fail(message);
        }
    }

    /**
     * @return version number part of JAR file
     */
    public String getJarVersion() {
        return JAR_NAME.replaceAll(".*([0-9]+\\.[0-9]+\\.[0-9]+).*", "$1");
    }
}
