package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.bucketfs.url.OsCheck.OSType;

class UriConverterTest {

    @ParameterizedTest
    @CsvSource(value = { //
            "relative,  dir/file.txt,  dir/file.txt", //
            "file only, file.txt,      file.txt", //
            "absolute,  /dir/file.txt, /dir/file.txt", //
    })
    void convert(final String testCase, final String input, final String expected) throws Exception {
        assertThat(convert(OSType.WINDOWS, input), equalTo(expected));
        assertThat(convert(OSType.LINUX, input), equalTo(expected));
    }

    @Test
    void driveLetter() throws Exception {
        final String input = "c:/dir/file.txt";
        assertThat(convert(OSType.WINDOWS, input), equalTo("file:///" + input));
        assertThat(convert(OSType.LINUX, input), equalTo(input));
    }

    private String convert(final OSType osType, final String input) throws URISyntaxException {
        final String path = (osType == OSType.WINDOWS) //
                ? input.replace('/', '\\')
                : input;
        return new UriConverter(osType).convert(path).toString();
    }

    @Test
    void nullUri() throws Exception {
        final UriConverter converter = new UriConverter();
        assertThrows(NullPointerException.class, () -> converter.convert((String) null));
    }
}
