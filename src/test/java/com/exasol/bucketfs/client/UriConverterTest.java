package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.bucketfs.client.OsCheck.OSType;

class UriConverterTest {

    @ParameterizedTest
    @CsvSource(value = { //
            "a/b/c.txt, a/b/c.txt", //
            "a\\b\\c.txt, a/b/c.txt", //
            "c:/a/b.txt, file:///c:/a/b.txt", //
            "file:c:/no-slash.txt, file:c:/no-slash.txt", //
            "file://c:/two-slashes.txt, file:///c:/two-slashes.txt", //
            "file:///c:/three-slashes.txt, file:///c:/three-slashes.txt", //
            "file:///c:\\backslash.txt, file:///c:/backslash.txt", //
    })
    void windows(final String uri, final String expected) throws Exception {
        final String actual = new UriConverter(OSType.WINDOWS).convert(uri).toString();
        assertThat(actual, equalTo(expected));
    }

    @Test
    void nullUri() throws Exception {
        final UriConverter converter = new UriConverter();
        assertThrows(NullPointerException.class, () -> converter.convert(null));
    }
}
