package com.exasol.bucketfs.profile;

import static com.exasol.bucketfs.Fallback.of;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.exasol.bucketfs.Fallback;
import com.exasol.errorreporting.ExaError;
import com.github.vincentrussell.ini.Ini;

/**
 * This class reads a profile from INI-file.
 */
public class ProfileReader implements ProfileProvider {

    /**
     * Create a new instance of {@link ProfileReader} for productive use.
     *
     * @param decodePasswords if {@code true} then profile reader should apply Base64 decoding to passwords
     */
    public static ProfileReader instance(final Boolean decodePasswords) {
        final String configFile = of(null, System.getProperty(CONFIG_FILE_PROPERTY), CONFIG_FILE);
        return new ProfileReader(Optional.ofNullable(decodePasswords), Path.of(configFile));
    }

    public static final String CONFIG_FILE_PROPERTY = "bfsc.config.file";

    private static final String HOME_DIRECTORY = System.getProperty("user.home");
    private static final Pattern BOOLEAN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    static final String CONFIG_FILE = HOME_DIRECTORY + "/.bucketfs-client/config.ini";

    private final Optional<Boolean> decodePasswords;
    private final Path configFile;

    /**
     * @param decodePasswords optional boolean if present and with value {@code true} then profile reader should apply
     *                        Base64 decoding to passwords
     */
    public ProfileReader(final Optional<Boolean> decodePasswords) {
        this(decodePasswords, Path.of(Fallback.of(null, System.getProperty(CONFIG_FILE_PROPERTY), CONFIG_FILE)));
    }

    ProfileReader(final Optional<Boolean> decodePasswords, final Path configFile) {
        this.decodePasswords = decodePasswords;
        this.configFile = configFile;
    }

    @Override
    public Profile getProfile(final String profileName) {
        try {
            return getProfileInternal(profileName);
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
                (String) section.get("password.write"), //
                this.decodePasswords.orElse(decodePasswords(section, "decode-base64")));
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
        return Profile.empty(this.decodePasswords.orElse(false));
    }

    private boolean decodePasswords(final Map<String, Object> section, final String key) {
        final String value = validate("boolean", this::validateBoolean, section, key);
        return value != null ? Boolean.valueOf(value) : false;
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

    private boolean validateBoolean(final String value) {
        if (!BOOLEAN.matcher(value).matches()) {
            throw new IllegalArgumentException();
        }
        return Boolean.valueOf(value);
    }
}
