package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.env.EnvironmentVariables.*;
import static com.exasol.bucketfs.url.BucketFsUrl.BUCKETFS_PROTOCOL;
import static com.exasol.bucketfs.url.BucketFsUrl.BUCKETFS_PROTOCOL_WITH_TLS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.*;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.exasol.bucketfs.env.EnvironmentVariables;

import nl.jqno.equalsverifier.EqualsVerifier;

// [utest->dsn~bucket-fs-url~1]

class BucketFsUrlTest {

    EnvironmentVariables env = EnvironmentVariables.from(Map.of(BUCKET, ""));
    EnvironmentVariables emptyEnv = EnvironmentVariables.from(Map.of());

    @CsvSource({ //
            "localhost, 8888, the_bucket, file.txt, false, bfs://localhost:8888/the_bucket/file.txt", //
    })
    @ParameterizedTest
    void createFromComponents(final String host, final int port, final String bucket, final String pathInBucket,
            final boolean useTls, final String expectedUri) throws Exception {
        final BucketFsUrl url = testee(host, port, bucket, pathInBucket, false);
        assertThat(url.toURI(), equalTo(URI.create(expectedUri)));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz" //
    })
    @ParameterizedTest
    void createFromString(final String spec) throws Exception {
        assertThat(testee(spec).toURI(), equalTo(URI.create(spec)));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz" //
    })
    @ParameterizedTest
    void createFromUri(final URI inputUri) throws Exception {
        assertThat(BucketFsUrl.from(inputUri, this.env).toURI(), equalTo(inputUri));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, /the_bucket/file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz, /b/foo/bar/baz" //
    })

    @ParameterizedTest
    void testGetPath(final String inputUri, final String expectedPath) throws Exception {
        assertThat(testee(inputUri).getPath(), equalTo(expectedPath));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, the_bucket", //
            "bfs://127.0.0.1:123/b/foo/bar/baz, b" //
    })
    @ParameterizedTest
    void testGetBucketName(final String inputUri, final String expectedBucketName) throws Exception {
        assertThat(testee(inputUri).getBucketName(), equalTo(expectedBucketName));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, /file.txt", //
            "bfs://127.0.0.1:123/b/foo/bar/baz, /foo/bar/baz" //
    })
    @ParameterizedTest
    void testGetPathInBucket(final String inputUri, final String expectedPath) throws Exception {
        assertThat(testee(inputUri).getPathInBucket(), equalTo(expectedPath));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_bucket/file.txt, 8888", //
            "bfs://127.0.0.1:123/b/foo/bar/baz, 123" //
    })
    @ParameterizedTest
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
            "bfs:/a/b.txt,              bfs://localhost:2580/default/a/b.txt", // nothing
            "bfs://1.2.3.4/a/b.txt,     bfs://1.2.3.4:2580/default/a/b.txt", // host
            "bfs://1.2.3.4:999/a/b.txt, bfs://1.2.3.4:999/default/a/b.txt", // host and port
    // port can only be specified together with host
    })
    void emptyEnvironment(final String spec, final String expected) throws Exception {
        verifyWithEnv(spec, this.emptyEnv, expected);
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "bfs:/a/b.txt,          bfs://host-from-environment:2580/default/a/b.txt",
            "bfs://1.2.3.4/a/b.txt, bfs://1.2.3.4:2580/default/a/b.txt", })
    void environmentHost(final String spec, final String expected) throws Exception {
        final EnvironmentVariables env = EnvironmentVariables.from(Map.of(HOST, "host-from-environment"));
        verifyWithEnv(spec, env, expected);
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "bfs:/a/b.txt,               bfs://localhost:9999/default/a/b.txt",
            "bfs://1.2.3.4:1234/a/b.txt, bfs://1.2.3.4:1234/default/a/b.txt", })
    void environmentPort(final String spec, final String expected) throws Exception {
        final EnvironmentVariables env = EnvironmentVariables.from(Map.of(PORT, "9999"));
        verifyWithEnv(spec, env, expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "bfs:/a/b.txt, bfs://localhost:2580/bucket-from-environment/a/b.txt" })
    void environmentBucket(final String spec, final String expected) throws Exception {
        final EnvironmentVariables env = EnvironmentVariables.from(Map.of(BUCKET, "bucket-from-environment"));
        verifyWithEnv(spec, env, expected);
    }

    @ParameterizedTest
    @ValueSource(strings = { "bfs:/my_bucket/a/b.txt", "bfs:///my_bucket/a/b.txt" })
    void bucketNameFromSpec(final String spec) throws Exception {
        verifyWithEnv(spec, this.env, "bfs://localhost:2580/my_bucket/a/b.txt");
    }

    @ParameterizedTest
    @ValueSource(strings = { "bfs:/drivers/a.txt" })
    void relative(final String spec) throws Exception {
        verifyWithEnv(spec, this.emptyEnv, "bfs://localhost:2580/default/drivers/a.txt");
    }

    // ------------------------------------------------

    private void verifyWithEnv(final String spec, final EnvironmentVariables env, final String expected)
            throws MalformedURLException, URISyntaxException {
        assertThat(testee(spec, env).toURI().toString(), equalTo(expected));
    }

    @Test
    void testEqualsContract() {
        EqualsVerifier.forClass(BucketFsUrl.class).withIgnoredFields("protocol", "host", "port", "bucketFsPath")
                .verify();
    }

    private BucketFsUrl testee(final String host, final int port, final String bucketName, final String pathInBucket,
            final boolean useTls) throws MalformedURLException, URISyntaxException {
        return new BucketFsUrl(calculateProtocol(useTls), host, port, new BucketFsPath(bucketName, "/" + pathInBucket));
    }

    private String calculateProtocol(final boolean useTls) {
        return useTls ? BUCKETFS_PROTOCOL_WITH_TLS : BUCKETFS_PROTOCOL;
    }

    private BucketFsUrl testee(final String spec) throws MalformedURLException, URISyntaxException {
        return testee(spec, this.env);
    }

    private BucketFsUrl testee(final String spec, final EnvironmentVariables env)
            throws MalformedURLException, URISyntaxException {
        return BucketFsUrl.from(new URI(spec), env);
    }
}