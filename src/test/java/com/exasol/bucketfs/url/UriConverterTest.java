package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.bucketfs.client.OsCheck.OSType;

class UriConverterTest {

    @ParameterizedTest
    @CsvSource(value = { //
            "relative,  dir/file.txt,  file://dir/file.txt", //
            "file only, file.txt,      file://file.txt", //
            "absolute,  /dir/file.txt, file:///dir/file.txt", //
    })
    void convert(final String testCase, final String input, final String expected) throws Exception {
        assertThat(convert(OSType.WINDOWS, input), equalTo(expected));
        assertThat(convert(OSType.LINUX, input), equalTo(expected));
    }

    @Test
    void driveLetter() throws Exception {
        final String input = "c:/dir/file.txt";
        assertThat(convert(OSType.WINDOWS, input), equalTo("file:///" + input));
        assertThat(convert(OSType.LINUX, input), equalTo("file://" + input));
    }

    private String convert(final OSType osType, final String input) throws URISyntaxException {
        final Path path = (osType == OSType.WINDOWS) //
                ? pathMock(input.replace('/', '\\'), input.startsWith("c:"))
                : pathMock(input, input.startsWith("/"));
        return new UriConverter(osType).convert(path).toString();
    }

    private Path pathMock(final String input, final boolean isAbsolute) {
        final Path result = mock(Path.class);
        when(result.toString()).thenReturn(input);
        when(result.isAbsolute()).thenReturn(isAbsolute);
        return result;
    }

    @Test
    void nullUri() throws Exception {
        final UriConverter converter = new UriConverter();
        assertThrows(NullPointerException.class, () -> converter.convert((String) null));
    }
}
