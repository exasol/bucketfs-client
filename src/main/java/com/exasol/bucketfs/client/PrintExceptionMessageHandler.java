package com.exasol.bucketfs.client;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * Handler that outputs exception messages as error messages on the consoles STDERR.
 */
public class PrintExceptionMessageHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(final Exception exception, final CommandLine commandLine,
            final ParseResult parseResult) throws Exception {
        commandLine.getErr().println(commandLine.getColorScheme().errorText(exception.getMessage()));
        return commandLine.getExitCodeExceptionMapper() != null
                ? commandLine.getExitCodeExceptionMapper().getExitCode(exception)
                : commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
}