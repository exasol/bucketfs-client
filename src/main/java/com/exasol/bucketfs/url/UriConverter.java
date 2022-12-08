package com.exasol.bucketfs.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.exasol.bucketfs.url.OsCheck.OSType;

import picocli.CommandLine.ITypeConverter;

/**
 * Converts String to URI even on windows OS
 */
public class UriConverter implements ITypeConverter<URI> {

    private static final String FILE = "file://";
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
        return fixDriveLetter(value.replace('\\', '/'));
    }

    private String fixDriveLetter(final String value) {
        return DRIVE_LETTER.matcher(value).find() //
                ? FILE + "/" + value
                : value;
    }

    private boolean isWindows() {
        return this.osType == OSType.WINDOWS;
    }
}
