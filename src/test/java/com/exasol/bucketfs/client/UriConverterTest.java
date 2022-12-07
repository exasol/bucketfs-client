package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.bucketfs.client.OsCheck.OSType;

class UriConverterTest {

    @ParameterizedTest
    @CsvSource(value = { //
            "a/b/c.txt, a/b/c.txt", //
            "a\\b\\c.txt, a/b/c.txt", //
            "file:c:/no-slash.txt, file:c:/no-slash.txt", //
            "file://c:/two-slashes.txt, file:///c:/two-slashes.txt", //
            "file:///c:/three-slashes.txt, file:///c:/three-slashes.txt", //
            "file:///c:\\backslash.txt, file:///c:/backslash.txt", //
    })

    void testWindowsUri(final String uri, final String expected) throws Exception {
        final String actual = new UriConverter(OSType.WINDOWS).convert(uri).toString();
        assertThat(actual, equalTo(expected));
    }
}
