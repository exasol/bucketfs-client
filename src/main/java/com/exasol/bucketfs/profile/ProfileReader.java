package com.exasol.bucketfs.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import com.exasol.bucketfs.Fallback;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.errorreporting.ExaError;
import com.github.vincentrussell.ini.Ini;

/**
 * This class reads a profile from INI-file.
 */
public class ProfileReader {

    public static final String CONFIG_FILE_PROPERTY = "bfsc.config.file";
    private static final String HOME_DIRECTORY = System.getProperty("user.home");
    static final String CONFIG_FILE = HOME_DIRECTORY + "/.bucketfs-client/config.ini";
    private final Path configFile;

    /**
     * Create a new instance of {@link ProfileReader} for productive use.
     */
    public ProfileReader() {
        this(Path.of(Fallback.of(null, System.getProperty(CONFIG_FILE_PROPERTY), CONFIG_FILE)));
    }

    ProfileReader(final Path configFile) {
        this.configFile = configFile;
    }

    Profile getProfile() {
        return getProfile(null);
    }

    /**
     * @param profileName name of the profile
     * @return {@link Profile} with default values for parts of {@link BucketFsUrl} and password.
     */
    public Profile getProfile(final String profileName) {
        try {
            return getProfileInternal(profileName != null ? profileName : "default");
        } catch (final IOException | IllegalArgumentException exception) {
            throw new IllegalStateException(ExaError.messageBuilder("E-BFSC-7") //
                    .message("Failed to read profile from {{file}} caused by {{cause|uq}}.", //
                            this.configFile, exception.getMessage()) //
                    .toString(), //
                    exception);
        }
    }

    private Profile getProfileInternal(final String profileName) throws IOException {
        final Map<String, Object> section = read().getSection(profileName);
        if (section == null) {
            return defaultProfile();
        }
        return new Profile( //
                (String) section.get("host"), //
                validate("integer", Integer::valueOf, section, "port"), //
                (String) section.get("bucket"), //
                (String) section.get("password.read"), //
                (String) section.get("password.write"));
    }

    Ini read() throws IOException {
        final Ini ini = new Ini();
        if (Files.notExists(this.configFile)) {
            return ini;
        }
        try (InputStream stream = Files.newInputStream(this.configFile)) {
            ini.load(stream);
            return ini;
        }
    }

    private Profile defaultProfile() {
        return Profile.empty();
    }

    private <T> String validate(final String datatype, final Function<String, T> validator,
            final Map<String, Object> section, final String key) {
        final String raw = stringValue(section.get(key));
        try {
            if (raw != null) {
                validator.apply(raw);
            }
            return raw;
        } catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException(String.format(//
                    "invalid %s value in entry '%s=%s'", datatype, key, raw));
        }
    }

    private String stringValue(final Object raw) {
        return raw != null ? String.valueOf(raw) : null;
    }

    Path getPathOfConfigFile() {
        return this.configFile;
    }
}
