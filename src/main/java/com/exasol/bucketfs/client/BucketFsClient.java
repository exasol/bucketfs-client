package com.exasol.bucketfs.client;

import java.util.concurrent.Callable;

import com.exasol.bucketfs.env.EnvironmentVariables;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

/**
 * This class implements the BucketFS client.
 */
// [impl->dsn~command-line-parsing~1]
@Command( //
        name = "bfsc", //
        description = "Exasol BucketFS client" //
)
public class BucketFsClient implements Callable<Integer> {
    @Spec
    CommandSpec spec;

    public static void main(final String[] arguments) {
        final EnvironmentVariables env = EnvironmentVariables.from(System.getenv());
        final CommandLine commandLineClient = new CommandLine(new BucketFsClient()) //
                .addSubcommand(new CopyCommand(env)) //
                .addSubcommand(new ListCommand(env)) //
                .setExecutionExceptionHandler(new PrintExceptionMessageHandler());
        final int exitCode = commandLineClient.execute(arguments);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        throw new ParameterException(this.spec.commandLine(), "Missing required subcommand");
    }
}