package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.profile.ProfileReader.CONFIG_FILE_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.exasol.containers.exec.ExitCode;

/**
 * Executes the BucketFsClient (BFSC) in integration tests.
 */
public class BFSC {

    private final String[] parameters;
    private Path configFile;
    private final List<String> consoleInput = new ArrayList<>();
    private int index = 0;
    private int expectedExitCode = ExitCode.OK;

    /**
     * Create the wrapper with the given parameters.
     *
     * @param parameters command line parameters
     * @return wrapper object
     */
    static BFSC create(final String... parameters) {
        return new BFSC(parameters);
    }

    private BFSC(final String[] parameters) {
        this.parameters = parameters;
    }

    BFSC withConfigFile(final Path path) {
        this.configFile = path;
        return this;
    }

    BFSC withExpectedExitCode(final int expectedExitCode) {
        this.expectedExitCode = expectedExitCode;
        return this;
    }

    /**
     * Feed STDIN with a string.
     *
     * @param line simulate this line to be entered via interactive console input
     * @return {@code this} for fluent programming
     */
    public BFSC feedStdIn(final String line) {
        this.consoleInput.add(line);
        return this;
    }

    private String simulateConsoleInput(final String prompt) {
        ConsoleReaderWithFallbackToStdIn.showPrompt(prompt);
        return this.index < this.consoleInput.size() //
                ? this.consoleInput.get(this.index++)
                : "";
    }

    /**
     * Run the BucketFS client.
     */
    public void run() {
        final String value = this.configFile != null ? this.configFile.toString() : "/non/existing/file";
        System.setProperty(CONFIG_FILE_PROPERTY, value);
        final int exitCode = BucketFsClient.mainWithConsoleReader(this::simulateConsoleInput, this.parameters);
        assertThat("exit code", exitCode, equalTo(expectedExitCode));
    }
}
