package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import nl.jqno.equalsverifier.EqualsVerifier;

class BucketFsUrlTest {
    @CsvSource({ //
            "localhost, 8888, the_service, the_bucket, file.txt, false, bfs://localhost:8888/the_service/the_bucket/file.txt", //
    })
    @ParameterizedTest
    void testCreateFromComponents(final String host, final int port, final String service, final String bucket,
            final String pathInBucket, final boolean useTls, final String expectedUri) throws MalformedURLException {
        final BucketFsUrl url = new BucketFsUrl(host, port, service, bucket, pathInBucket, false);
        assertThat(url.toURI(), equalTo(URI.create(expectedUri)));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz" //
    })
    @ParameterizedTest
    void testCreateFromString(final String inputUri) throws MalformedURLException {
        assertThat(new BucketFsUrl(inputUri).toURI(), equalTo(URI.create(inputUri)));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, /the_service/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, /s/b/foo/bar/baz" //
    })
    @ParameterizedTest
    void testGetPath(final String inputUri, final String expectedPath) throws MalformedURLException {
        assertThat(new BucketFsUrl(inputUri).getPath(), equalTo(expectedPath));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, 8888", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, 123" //
    })
    @ParameterizedTest
    void testGetPort(final String inputUri, final int expectedPath) throws MalformedURLException {
        assertThat(new BucketFsUrl(inputUri).getPort(), equalTo(expectedPath));
    }

    @Test
    void testGetDefaultPortWithoutTls() {
        assertThat(createRandomBucketFsUrl(false).getDefaultPort(), equalTo(2580));
    }

    private BucketFsUrl createRandomBucketFsUrl(final boolean useTls) {
        return new BucketFsUrl("foo", 1234, "service", "bucket", "/path", useTls);
    }

    @Test
    void testGetDefaultPortWithTls() {
        assertThat(createRandomBucketFsUrl(true).getDefaultPort(), equalTo(-1));
    }

    @Test
    void testGetProtocolWithouTls() {
        assertThat(createRandomBucketFsUrl(false).getProtocol(), equalTo("bfs"));
    }

    @Test
    void testGetProtocolWithTls() {
        assertThat(createRandomBucketFsUrl(true).getProtocol(), equalTo("bfss"));
    }

    @CsvSource({ //
            "bfs://localhost:777/a/b/c, localhost", //
            "bfs://192.168.1.1/a/b/c, 192.168.1.1" })
    @ParameterizedTest
    void testGetHost(final String inputUri, final String expectedHost) throws MalformedURLException {
        assertThat(new BucketFsUrl(inputUri).getHost(), equalTo(expectedHost));
    }

    @CsvSource({ //
            "localhost, 8888, the_service, the_bucket, file.txt, false, bfs://localhost:8888/the_service/the_bucket/file.txt", //
    })
    @ParameterizedTest
    void testToString(final String host, final int port, final String service, final String bucket,
            final String pathInBucket, final boolean useTls, final String expectedUri) throws MalformedURLException {
        final BucketFsUrl url = new BucketFsUrl(host, port, service, bucket, pathInBucket, false);
        assertThat(url.toString(), equalTo(expectedUri));
    }

    @Test
    void testEqualsContract() {
        EqualsVerifier.forClass(BucketFsUrl.class).verify();
    }
}