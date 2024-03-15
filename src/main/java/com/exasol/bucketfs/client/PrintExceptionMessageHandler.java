package com.exasol.bucketfs.client;

import java.util.Objects;

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
        if (Objects.isNull(exception.getCause())) {
            commandLine.getErr().println(commandLine.getColorScheme().errorText(exception.getMessage()));
        } else {
            commandLine.getErr().println(commandLine.getColorScheme()
                    .errorText(exception.getMessage() + ". Cause: " + exception.getCause().getMessage()));

        }
        return commandLine.getExitCodeExceptionMapper() != null
                ? commandLine.getExitCodeExceptionMapper().getExitCode(exception)
                : commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
}