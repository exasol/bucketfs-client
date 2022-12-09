package com.exasol.bucketfs.client;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

// [impl->dsn~command-line-parsing~1]
@Command( //
        name = "bfs", //
        subcommands = { CopyCommand.class, ListCommand.class }, //
        description = "Exasol BucketFS client" //
)
public class BucketFsClient implements Callable<Integer> {
    @Spec
    CommandSpec spec;

    public static void main(final String[] arguments) {
        final CommandLine commandLineClient = new CommandLine(new BucketFsClient())
                .setExecutionExceptionHandler(new PrintExceptionMessageHandler());
        final int exitCode = commandLineClient.execute(arguments);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        throw new ParameterException(this.spec.commandLine(), "Missing required subcommand");
    }
}