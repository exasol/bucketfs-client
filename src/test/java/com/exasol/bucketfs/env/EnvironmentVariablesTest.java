package com.exasol.bucketfs.env;

import static com.exasol.bucketfs.env.EnvironmentVariables.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.url.BucketFsUrl;

class EnvironmentVariablesTest {

    @Test
    void emptyEnvironment() {
        final EnvironmentVariables testee = testee(Map.of());
        assertThat(testee.host(), nullValue());
        assertThat(testee.port(), equalTo(BucketFsUrl.UNDEFINED_PORT));
        assertThat(testee.bucket(), nullValue());
        assertThat(testee.password(), nullValue());
    }

    @Test
    void host() {
        verifyValue(HOST, EnvironmentVariables::host);
    }

    @Test
    void port() {
        final EnvironmentVariables testee = testee(Map.of(PORT, "123"));
        assertThat(testee.port(), equalTo(123));
    }

    @Test
    void bucket() {
        verifyValue(BUCKET, EnvironmentVariables::bucket);
    }

    @Test
    void password() {
        verifyValue(PASSWORD, EnvironmentVariables::password);
    }

    private void verifyValue(final String variableName, final Function<EnvironmentVariables, String> accessor) {
        final String value = "sample value";
        final EnvironmentVariables testee = testee(Map.of(variableName, value));
        final String actual = accessor.apply(testee);
        assertThat(actual, equalTo(value));
    }

    private EnvironmentVariables testee(final Map<String, String> map) {
        return EnvironmentVariables.from(map);
    }
}
