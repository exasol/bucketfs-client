package com.exasol.bucketfs.profile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import com.exasol.bucketfs.Fallback;
import com.exasol.errorreporting.ExaError;
import com.github.vincentrussell.ini.Ini;

/**
 * This class reads a profile from INI-file
 */
public class ProfileReader implements ProfileProvider {

    public static final String CONFIG_FILE_PROPERTY = "bfsc.properties.file";

    private static final String HOME_DIRECTORY = System.getProperty("user.home");
    static final String CONFIG_FILE = HOME_DIRECTORY + "/.bucketfs-client/config";

    private final Path configFile;
    Optional<Ini> ini;

    public ProfileReader() {
        this(Path.of(Fallback.fallback(null, System.getProperty(CONFIG_FILE_PROPERTY), CONFIG_FILE)));
    }

    /**
     * @param configFile
     */
    public ProfileReader(final Path configFile) {
        this.configFile = configFile;
    }

    Optional<Ini> read() {
        if (Files.notExists(this.configFile)) {
            return Optional.empty();
        }
        try (InputStream stream = Files.newInputStream(this.configFile)) {
            return Optional.of(read(stream));
        } catch (final IOException exception) {
            throw new UncheckedIOException(ExaError.messageBuilder("E-BFSC-7") //
                    .message("Failed to read profile from {{file}}", this.configFile) //
                    .message("caused by {{cause}}", exception.getMessage()) //
                    .toString(), //
                    exception);
        }
    }

    Ini read(final InputStream stream) throws IOException {
        final Ini ini = new Ini();
        ini.load(stream);
        return ini;
    }

    @Override
    public Profile getProfile(final String profileName) {
        if (this.ini == null) {
            this.ini = read();
        }
        if (this.ini.isEmpty()) {
            return Profile.empty();
        }
        final Map<String, Object> section = this.ini.get().getSection(profileName);
        if (section == null) {
            return Profile.empty();
        }
        return new Profile( //
                (String) section.get("host"), //
                string((Long) section.get("port")), //
                (String) section.get("bucket"), //
                (String) section.get("password"));
    }

    private String string(final Long port) {
        return port == null ? null : port.toString();
    }

    Path getPathOfConfigFile() {
        return this.configFile;
    }
}
