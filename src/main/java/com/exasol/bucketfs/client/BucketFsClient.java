package com.exasol.bucketfs.client;

import java.util.concurrent.Callable;

import com.exasol.bucketfs.client.PasswordReader.ConsoleReader;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.profile.ProfileReader;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

/**
 * This class implements the BucketFS client.
 */
// [impl->dsn~command-line-parsing~1]
@Command( //
        name = "bfsc", //
        mixinStandardHelpOptions = true, //
        description = "Exasol BucketFS client", //
        subcommands = { CopyCommand.class, ListCommand.class, DeleteCommand.class })
public class BucketFsClient implements Callable<Integer> {
    @Spec
    private CommandSpec spec;
    @Option(names = { "-r", "--recursive" }, description = "recursive", scope = ScopeType.INHERIT)
    private boolean recursive;
    @Option(names = { "-p", "--profile" }, description = "name of the profile to use", scope = ScopeType.INHERIT)
    private String profileName;
    @Option(names = { "-pw", "--require-read-password" }, //
            description = "whether BFSC should ask for a read password", scope = ScopeType.INHERIT)
    private boolean requireReadPassword;

    private final ConsoleReader consoleReader;
    private Profile profile;

    public static void main(final String[] arguments) {
        mainWithConsoleReader(PasswordReader.defaultConsoleReader(), arguments);
    }

    static void mainWithConsoleReader(final ConsoleReader consoleReader, final String[] arguments) {
        final CommandLine commandLineClient = new CommandLine(new BucketFsClient(consoleReader)) //
                .setExecutionExceptionHandler(new PrintExceptionMessageHandler());
        final int exitCode = commandLineClient.execute(arguments);
        System.exit(exitCode);
    }

    public BucketFsClient(final ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }

    String recursiveOption() {
        return String.join(" or ", this.spec.findOption("-r").names());
    }

    void printWarning(final String message) {
        final CommandLine cl = this.spec.commandLine();
        cl.getErr().println(cl.getColorScheme().errorText(message));
    }

    void print(final String message) {
        final CommandLine cl = this.spec.commandLine();
        cl.getOut().println(message);
    }

    boolean isRecursive() {
        return this.recursive;
    }

    public Profile getProfile() {
        if (this.profile == null) {
            this.profile = new ProfileReader().getProfile(this.profileName);
        }
        return this.profile;
    }

    String readPassword() {
        return PasswordReader.forReading(this.requireReadPassword, this.consoleReader) //
                .readPassword(getProfile());
    }

    String writePassword() {
        return PasswordReader.forWriting(this.consoleReader).readPassword(getProfile());
    }

    @Override
    public Integer call() {
        throw new ParameterException(this.spec.commandLine(), "Missing required subcommand");
    }
}
