package com.exasol.bucketfs.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.exasol.bucketfs.client.OsCheck;
import com.exasol.bucketfs.client.OsCheck.OSType;

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

    /**
     * @param path path to return URI for
     * @return {@link URI} instance
     * @throws URISyntaxException if format of path does not conform to valid URI
     */
    public URI convert(final Path path) throws URISyntaxException {
        return new URI(prefix(path) + suffix(path.toString()));
    }

    private String prefix(final Path path) {
        final String slash = isWindows() && path.isAbsolute() ? "/" : "";
        return FILE + slash;
    }

    private String suffix(final String path) {
        return isWindows() ? path.replace('\\', '/') : path;
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
