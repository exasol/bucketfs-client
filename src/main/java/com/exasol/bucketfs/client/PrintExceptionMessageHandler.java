package com.exasol.bucketfs.client;

import java.util.Objects;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * Handler that outputs exception messages as error messages on the consoles STDERR.
 */
public class PrintExceptionMessageHandler implements IExecutionExceptionHandler {
    private static final int MAX_DEPTH = 5;

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
        return message + getNestedCause(exception);
    }

    private static String getNestedCause(final Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        final StringBuilder message = new StringBuilder();
        Throwable cause = throwable.getCause();
        int depth = 0;
        while (cause != null && depth++ < MAX_DEPTH) {
            message.append(", Cause: " + cause.toString());
            cause = cause.getCause();
        }
        return message.toString();
    }

    private static boolean hasConnectException(final Exception exception) {
        Throwable cause = exception.getCause();
        int depth = 0;
        while (cause != null && depth++ < 10) {
            if (cause instanceof java.net.ConnectException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
