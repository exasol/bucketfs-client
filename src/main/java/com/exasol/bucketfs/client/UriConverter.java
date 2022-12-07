package com.exasol.bucketfs.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.exasol.bucketfs.client.OsCheck.OSType;

import picocli.CommandLine.ITypeConverter;

/**
 * Converts String to URI even on windows OS
 */
public class UriConverter implements ITypeConverter<URI> {

    private static final String FILE = "file://";
    private static final Pattern SCHEME_PATTERN = Pattern.compile("^(" + FILE + "|" + FILE + "[^/].*)$");
    private static final Pattern DRIVE_LETTER = Pattern.compile("^[a-z]:/", Pattern.CASE_INSENSITIVE);

    private final OSType osType;

    /**
     * Constructor
     */
    public UriConverter() {
        this(new OsCheck().getOperatingSystemType());
    }

    UriConverter(final OSType osType) {
        this.osType = osType;
    }

    @Override
    public URI convert(final String value) throws URISyntaxException {
        return new URI(normalize(value));
    }

    private String normalize(final String value) {
        if ((value == null) || !isWindows()) {
            return value;
        }

        final String result = value.replace('\\', '/');
        if (DRIVE_LETTER.matcher(result).find()) {
            return FILE + "/" + result;
        }
        if (SCHEME_PATTERN.matcher(result).matches()) {
            return result.replace(FILE, FILE + "/");
        }
        return result;
    }

    private boolean isWindows() {
        return this.osType == OSType.WINDOWS;
    }
}
