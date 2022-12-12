package com.exasol.bucketfs.profile;

import static com.exasol.bucketfs.Lines.lines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ProfileReaderTest {

    @Test
    void defaultConfigFile() {
        final ProfileReader testee = new ProfileReader();
        assertThat(testee.getPathOfConfigFile(), equalTo(Path.of(ProfileReader.CONFIG_FILE)));
    }

    @Test
    @ClearSystemProperty(key = ProfileReader.CONFIG_FILE_PROPERTY)
    void configFileFromSystemProperty() {
        final Path path = Path.of("/sample/location/file");
        System.setProperty(ProfileReader.CONFIG_FILE_PROPERTY, path.toString());
        final ProfileReader testee = new ProfileReader();
        assertThat(testee.getPathOfConfigFile(), equalTo(path));
    }

    @Test
    void nonExistingFile() {
        final ProfileReader testee = new ProfileReader(Path.of("/non/existing/file"));
        assertThat(testee.getProfile(), equalTo(Profile.empty()));
    }

    @Test
    void noDefaultProfile(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("file");
        Files.writeString(file, "[xxx]");
        final ProfileReader testee = new ProfileReader(file);
        assertThat(testee.getProfile(), equalTo(Profile.empty()));
    }

    @Test
    void success(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("file");
        final String host = "host-from-profile";
        final String port = "8888";
        final String bucket = "bucket-from-profile";
        final String password = "password-from-profile";
        Files.writeString(file, lines("[default]", //
                "host=" + host, //
                "port=" + port, //
                "bucket=" + bucket, //
                "password=" + password));
        final ProfileReader testee = new ProfileReader(file);
        assertThat(testee.getProfile(), equalTo(new Profile( //
                host, //
                port, //
                bucket, //
                password)));
    }

}
