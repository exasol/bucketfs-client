package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BucketFsProtocolTest {
    @Test
    void nameNotFound() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> BucketFsProtocol.forName("unsupported"));
        assertThat(exception.getMessage(),
                equalTo("E-BFSC-17: Unsupported BucketFS protocol 'unsupported'. Use one of 'bfs' or 'bfss'."));
    }

    @ParameterizedTest
    @CsvSource({ "bfs, BFS", "bfss, BFSS" })
    void nameFound(final String name, final BucketFsProtocol expectedProtocol) {
        assertThat(BucketFsProtocol.forName(name), equalTo(expectedProtocol));
    }
}
