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
        commandLine.getErr().println(commandLine.getColorScheme().errorText(getErrorMessage(exception)));
        return commandLine.getExitCodeExceptionMapper() != null
                ? commandLine.getExitCodeExceptionMapper().getExitCode(exception)
                : commandLine.getCommandSpec().exitCodeOnExecutionException();
    }

    static String getErrorMessage(final Exception exception) {
        return exception.getMessage() + getCauseMessage(exception);
    }

    private static String getCauseMessage(final Exception exception) {
        if (Objects.isNull(exception.getCause())) {
            return "";
        }
        String message = "";
        if (hasConnectException(exception)) {
            message = ". Unable to connect to service";
        }
        return message + getNestedCause(exception.getCause());
    }

    private static String getNestedCause(final Throwable cause) {
        if (cause == null) {
            return "";
        }
        return ", Cause: " + cause.toString() + getNestedCause(cause.getCause());
    }

    private static boolean hasConnectException(final Exception exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof java.net.ConnectException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
