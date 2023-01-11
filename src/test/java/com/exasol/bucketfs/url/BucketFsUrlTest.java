package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.url.BucketFsUrl.BUCKETFS_PROTOCOL;
import static com.exasol.bucketfs.url.BucketFsUrl.BUCKETFS_PROTOCOL_WITH_TLS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.exasol.bucketfs.profile.Profile;

import nl.jqno.equalsverifier.EqualsVerifier;

// [utest->dsn~bucket-fs-url~1]

class BucketFsUrlTest {

    Profile profile = Profile.empty(false);

    @ParameterizedTest
    @CsvSource({ //
            "localhost, 8888, the_bucket, file.txt, false, bfs://localhost:8888/the_bucket/file.txt", //
    })
    void createFromComponents(final String host, final int port, final String bucket, final String pathInBucket,
            final boolean useTls, final String expectedUri) throws Exception {
        final BucketFsUrl url = testee(host, port, bucket, pathInBucket, false);
        assertThat(url.toString(), equalTo(expectedUri));
    }

    @ParameterizedTest
    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz" //
    })
    void createFromString(final String spec) throws Exception {
        assertThat(testee(spec).toString(), equalTo(spec));
    }

    @ParameterizedTest
    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz" //
    })
    void createFromUri(final String spec) throws Exception {
        assertThat(BucketFsUrl.from(URI.create(spec), this.profile).toString(), equalTo(spec));
    }

    @ParameterizedTest
    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, the_bucket", //
            "bfs://127.0.0.1:123/b/foo/bar/baz, b" //
    })
    void testGetBucketName(final String inputUri, final String expectedBucketName) throws Exception {
        assertThat(testee(inputUri).getBucketName(), equalTo(expectedBucketName));
    }

    @ParameterizedTest
    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, /file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz,        /foo/bar/baz", //
            "bfs://127.0.0.1:123/b/foo/bar/baz/,       /foo/bar/baz/" //
    })
    void testGetPathInBucket(final String inputUri, final String expectedPath) throws Exception {
        assertThat(testee(inputUri).getPathInBucket(), equalTo(expectedPath));
    }

    @ParameterizedTest
    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, 8888", //
            "bfs://127.0.0.1:123/b/foo/bar/baz, 123" //
    })
    void testGetPort(final String inputUri, final int expectedPath) throws Exception {
        assertThat(testee(inputUri).getPort(), equalTo(expectedPath));
    }

    @Test
    void testGetDefaultPortWithoutTls() throws Exception {
        assertThat(randomBucketFsUrl(false).getDefaultPort(), equalTo(2580));
    }

    @Test
    void testGetDefaultPortWithTls() throws Exception {
        assertThat(randomBucketFsUrl(true).getDefaultPort(), equalTo(-1));
    }

    @Test
    void testGetProtocolWithouTls() throws Exception {
        assertThat(randomBucketFsUrl(false).getProtocol(), equalTo("bfs"));
    }

    @Test
    void testGetProtocolWithTls() throws Exception {
        assertThat(randomBucketFsUrl(true).getProtocol(), equalTo("bfss"));
    }

    private BucketFsUrl randomBucketFsUrl(final boolean useTls) throws Exception {
        return testee("foo", 1234, "bucket", "/path", useTls);
    }

    @CsvSource({ //
            "bfs://localhost:777/a/b, localhost", //
            "bfs://192.168.1.1/a/b, 192.168.1.1" })
    @ParameterizedTest
    void testGetHost(final String inputUri, final String expectedHost) throws Exception {
        assertThat(testee(inputUri).getHost(), equalTo(expectedHost));
    }

    @CsvSource({ //
            "localhost, 8888, the_bucket, file.txt, false, bfs://localhost:8888/the_bucket/file.txt", //
    })
    @ParameterizedTest
    void testToString(final String host, final int port, final String bucket, final String pathInBucket,
            final boolean useTls, final String expectedUri) throws Exception {
        final BucketFsUrl url = testee(host, port, bucket, pathInBucket, false);
        assertThat(url.toString(), equalTo(expectedUri));
    }

    @CsvSource({ //
            "bfs://a/b/, false", //
            "bfss://a/b/, true" //
    })
    @ParameterizedTest
    void testIsTlsEnabled(final String inputUri, final boolean useTls) throws Exception {
        assertThat(testee(inputUri).isTlsEnabled(), equalTo(useTls));
    }

    @CsvSource({ //
            ", false", // Null check
            "bfs:/, true", //
            "bfss:/, true", //
            "bfs, false", //
            "bfss, false", //
            "BFS:/, false", //
            "http://www.example.com, false", //
            "file:///foo.bar, false" })
    @ParameterizedTest
    void testIsBucketFsUrl(final URI inputUri, final boolean expectedEvaluation) {
        assertThat(BucketFsUrl.isBucketFsUrl(inputUri), equalTo(expectedEvaluation));
    }

    /**
     * TODO: Test path without port and unset environment variable default port and TLS / protocol bfss:
     */

    @ParameterizedTest
    @CsvSource(value = { //
            "bfs:/bucket/file,               bfs://localhost:2580/bucket/file", // nothing
            "bfs://1.2.3.4/bucket/file,      bfs://1.2.3.4:2580/bucket/file", // host
            "bfs://1.2.3.4:9999/bucket/file, bfs://1.2.3.4:9999/bucket/file", // host and port
    // port can only be specified together with host
    })
    void emptyEnvironment(final String spec, final String expected) throws Exception {
        verifyWithEnv(spec, this.profile, expected);
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "bfs:/bucket/file,          bfs://host-from-profile:2580/bucket/file",
            "bfs://1.2.3.4/bucket/file, bfs://1.2.3.4:2580/bucket/file", })
    void hostFromProfile(final String spec, final String expected) throws Exception {
        final Profile profile = new Profile("host-from-profile", null, null, null, null, false);
        verifyWithEnv(spec, profile, expected);
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "bfs:/bucket/file,               bfs://localhost:9999/bucket/file",
            "bfs://1.2.3.4:1234/bucket/file, bfs://1.2.3.4:1234/bucket/file", })
    void portFromProfile(final String spec, final String expected) throws Exception {
        final Profile profile = new Profile(null, "9999", null, null, null, false);
        verifyWithEnv(spec, profile, expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "bfs:/a/b.txt, bfs://localhost:2580/bucket-from-environment/a/b.txt" })
    void bucketFromProfile(final String spec, final String expected) throws Exception {
        final Profile profile = new Profile(null, null, "bucket-from-environment", null, null, false);
        verifyWithEnv(spec, profile, expected);
    }

    @ParameterizedTest
    @ValueSource(strings = { "bfs:/bucket/drivers/a.txt" })
    void relative(final String spec) throws Exception {
        verifyWithEnv(spec, this.profile, "bfs://localhost:2580/bucket/drivers/a.txt");
    }

    private void verifyWithEnv(final String spec, final Profile profile, final String expected)
            throws MalformedURLException, URISyntaxException {
        assertThat(testee(spec, profile).toString(), equalTo(expected));
    }

    @Test
    void testEqualsContract() {
        EqualsVerifier.forClass(BucketFsUrl.class).verify();
    }

    private BucketFsUrl testee(final String host, final int port, final String bucketName, final String pathInBucket,
            final boolean useTls) throws MalformedURLException, URISyntaxException {
        return new BucketFsUrl(calculateProtocol(useTls), host, port, new BucketFsPath(bucketName, "/" + pathInBucket));
    }

    private String calculateProtocol(final boolean useTls) {
        return useTls ? BUCKETFS_PROTOCOL_WITH_TLS : BUCKETFS_PROTOCOL;
    }

    private BucketFsUrl testee(final String spec) throws MalformedURLException, URISyntaxException {
        return testee(spec, this.profile);
    }

    private BucketFsUrl testee(final String spec, final Profile profile)
            throws MalformedURLException, URISyntaxException {
        return BucketFsUrl.from(new URI(spec), profile);
    }
}