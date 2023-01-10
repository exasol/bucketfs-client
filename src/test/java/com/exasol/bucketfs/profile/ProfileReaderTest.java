package com.exasol.bucketfs.profile;

import static com.exasol.bucketfs.Lines.lines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ProfileReaderTest {

    @Test
    void testDefaultConfigFile() {
        assertThat(ProfileReader.instance(null).getPathOfConfigFile(), equalTo(Path.of(ProfileReader.CONFIG_FILE)));
    }

    @Test
    @ClearSystemProperty(key = ProfileReader.CONFIG_FILE_PROPERTY)
    void testConfigFileFromSystemProperty() {
        final Path path = Path.of("/sample/location/file");
        System.setProperty(ProfileReader.CONFIG_FILE_PROPERTY, path.toString());
        assertThat(ProfileReader.instance(null).getPathOfConfigFile(), equalTo(path));
    }

    @Test
    void testNonExistingFile() {
        final ProfileReader testee = testee(Path.of("/non/existing/file"));
        assertThat(testee.getProfile(), equalTo(Profile.empty(false)));
    }

    @Test
    void testNoDefaultProfile(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("file");
        Files.writeString(file, "[xxx]");
        assertThat(testee(file).getProfile(), equalTo(Profile.empty(false)));
    }

    @Test
    void testInvalidPort(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("file");
        Files.writeString(file, lines("[default]", "port=abc"));
        final ProfileReader testee = testee(file);
        final Exception exception = assertThrows(IllegalStateException.class, () -> testee.getProfile());
        assertThat(exception.getMessage(), matchesRegex("E-BFSC-7: Failed to read profile from '.*'"
                + " caused by invalid integer value in entry 'port=abc'."));
    }

    @Test
    void testInvalidDecodeOption(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("file");
        Files.writeString(file, lines("[default]", "decode-base64=abc"));
        final ProfileReader testee = testee(file);
        final Exception exception = assertThrows(IllegalStateException.class, () -> testee.getProfile());
        assertThat(exception.getMessage(), matchesRegex("E-BFSC-7: Failed to read profile from '.*'"
                + " caused by invalid boolean value in entry 'decode-base64=abc'."));
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "     , true , true", //
            "     , false, false", //
            "     ,      , false", //
            "true , false, true", //
            "false, true , false", //
            "false, null , false", //
            "true , null , true", //
    })
    void testCommandLineOverridesProfile(final Boolean cmdline, final Boolean profileValue, final boolean expected,
            @TempDir final Path tempDir) throws IOException {
        final String readPassword = "read-password";
        final String writePassword = "write-password";
        final Path file = tempDir.resolve("file");
        final String encoded = encode(readPassword, expected);
        Files.writeString(file, lines("[default]", //
                "password.read=" + encoded, //
                "password.write=" + encode(writePassword, expected), //
                profileValue != null ? "decode-base64=" + profileValue : ""));
        final Profile profile = new ProfileReader(Optional.ofNullable(cmdline), file).getProfile();
        assertThat(profile.decodePasswords(), is(expected));
        assertThat(profile.readPassword(), equalTo(readPassword));
        assertThat(profile.writePassword(), equalTo(writePassword));
    }

    private String encode(final String raw, final boolean encode) {
        return encode ? new String(Base64.getEncoder().encode(raw.getBytes())) : raw;
    }

    @Test
    void testSuccess(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("file");
        final String host = "host-from-profile";
        final String port = "8888";
        final String bucket = "bucket-from-profile";
        final String readPassword = "read-password-from-profile";
        final String writePassword = "write-password-from-profile";
        final String decode = "true";
        Files.writeString(file, lines("[default]", //
                "host=" + host, //
                "port=" + port, //
                "bucket=" + bucket, //
                "password.read=" + readPassword, //
                "password.write=" + writePassword, //
                "decode-base64=" + decode));
        assertThat(testee(file).getProfile(), equalTo(new Profile( //
                host, //
                port, //
                bucket, //
                readPassword, //
                writePassword, //
                Boolean.valueOf(decode))));
    }

    private ProfileReader testee(final Path file) {
        return new ProfileReader(Optional.empty(), file);
    }
}
