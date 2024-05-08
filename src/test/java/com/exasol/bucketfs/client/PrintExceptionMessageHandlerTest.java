package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.ConnectException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrintExceptionMessageHandlerTest {

    static Stream<Arguments> exceptionMessageTestData() {
        return Stream.of( //
                testCase("no cause", new Exception("msg"), "msg"), //
                testCase("single cause", new Exception("msg", new Exception("cause msg")),
                        "msg, Cause: java.lang.Exception: cause msg"), //
                testCase("nested cause",
                        new Exception("msg", new IOException("cause msg", new RuntimeException("nested cause msg"))),
                        "msg, Cause: java.io.IOException: cause msg, Cause: java.lang.RuntimeException: nested cause msg"), //
                testCase("direct cause connect exception", new Exception("msg", new ConnectException("cause msg")),
                        "msg. Unable to connect to service, Cause: java.net.ConnectException: cause msg"), //
                testCase("indirect cause connect exception",
                        new Exception("msg", new RuntimeException("cause 1", new ConnectException("cause 2 msg"))),
                        "msg. Unable to connect to service, Cause: java.lang.RuntimeException: cause 1, Cause: java.net.ConnectException: cause 2 msg") //
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("exceptionMessageTestData")
    void testGetErrorMessage(final String testName, final Exception exception, final String expectedMessage) {
        assertThat(PrintExceptionMessageHandler.getErrorMessage(exception), equalTo(expectedMessage));
    }

    @Test
    void testStopsAtDepthFive() {
        final RuntimeException cause1 = new RuntimeException("cause 1");
        final RuntimeException cause2 = new RuntimeException("cause 2", cause1);
        cause1.initCause(cause2);
        final Exception exception = new Exception("msg", cause1);
        assertThat(PrintExceptionMessageHandler.getErrorMessage(exception), equalTo(
                "msg, Cause: java.lang.RuntimeException: cause 1, Cause: java.lang.RuntimeException: cause 2, Cause: java.lang.RuntimeException: cause 1, Cause: java.lang.RuntimeException: cause 2, Cause: java.lang.RuntimeException: cause 1"));
    }

    static Arguments testCase(final String testName, final Exception exception, final String expectedMessage) {
        return Arguments.of(testName, exception, expectedMessage);
    }
}
