package com.exasol.bucketfs.profile;

import static com.exasol.bucketfs.Lines.lines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.ClearSystemProperty;

import com.exasol.bucketfs.url.BucketFsProtocol;

class ProfileReaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void testDefaultConfigFile() {
        assertThat(new ProfileReader().getPathOfConfigFile(), equalTo(Path.of(ProfileReader.CONFIG_FILE)));
    }

    @Test
    @ClearSystemProperty(key = ProfileReader.CONFIG_FILE_PROPERTY)
    void testConfigFileFromSystemProperty() {
        final Path path = Path.of("/sample/location/file");
        System.setProperty(ProfileReader.CONFIG_FILE_PROPERTY, path.toString());
        assertThat(new ProfileReader().getPathOfConfigFile(), equalTo(path));
    }

    @Test
    void testNonExistingFile() {
        final ProfileReader testee = testee(Path.of("/non/existing/file"));
        assertThat(testee.getProfile(), equalTo(Profile.empty()));
    }

    @Test
    void testNoDefaultProfile() throws IOException {
        final Path file = this.tempDir.resolve("file");
        Files.writeString(file, "[xxx]");
        assertThat(testee(file).getProfile(), equalTo(Profile.empty()));
    }

    @Test
    void testInvalidPort() throws IOException {
        final Path file = this.tempDir.resolve("file");
        Files.writeString(file, lines("[default]", "port=abc"));
        final ProfileReader testee = testee(file);
        final Exception exception = assertThrows(IllegalStateException.class, () -> testee.getProfile());
        assertThat(exception.getMessage(), matchesRegex("E-BFSC-7: Failed to read profile from '.*'"
                + " caused by invalid integer value in entry 'port=abc'."));
    }

    @Test
    void testSuccess() throws IOException {
        final Path file = this.tempDir.resolve("file");
        final String host = "host-from-profile";
        final String port = "8888";
        final String bucket = "bucket-from-profile";
        final String readPassword = "read-password-from-profile";
        final String writePassword = "write-password-from-profile";
        final BucketFsProtocol protocol = BucketFsProtocol.BFS;
        final Path tlsCertificatePath = Path.of("/path/to/cert.pem");
        Files.writeString(file, lines("[default]", //
                "host=" + host, //
                "port=" + port, //
                "bucket=" + bucket, //
                "protocol=" + protocol.getName(), //
                "certificate=" + tlsCertificatePath.toString(), //
                "password.read=" + readPassword, //
                "password.write=" + writePassword));
        assertThat(testee(file).getProfile(), equalTo(Profile.builder()
                .protocol(protocol)
                .host(host)
                .port(port)
                .bucket(bucket)
                .readPassword(readPassword)
                .writePassword(writePassword)
                .tlsCertificate(tlsCertificatePath)
                .build()));
    }

    private ProfileReader testee(final Path file) {
        return new ProfileReader(file);
    }
}
