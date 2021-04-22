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
        assertThat(BucketFsUrl.create(inputUri).toURI(), equalTo(URI.create(inputUri)));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz" //
    })
    @ParameterizedTest
    void testCreateFromUri(final URI inputUri) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).toURI(), equalTo(inputUri));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, /the_service/the_bucket/file.txt", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, /s/b/foo/bar/baz" //
    })

    @ParameterizedTest
    void testGetPath(final String inputUri, final String expectedPath) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).getPath(), equalTo(expectedPath));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, the_service", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, s" //
    })
    @ParameterizedTest
    void testGetServiceName(final String inputUri, final String expectedServiceName) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).getServiceName(), equalTo(expectedServiceName));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, the_bucket", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, b" //
    })
    @ParameterizedTest
    void testGetBucketName(final String inputUri, final String expectedBucketName) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).getBucketName(), equalTo(expectedBucketName));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, /file.txt", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, /foo/bar/baz" //
    })
    @ParameterizedTest
    void testGetPathInBucket(final String inputUri, final String expectedPath) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).getPathInBucket(), equalTo(expectedPath));
    }

    @CsvSource({ //
            "bfs://localhost:8888/the_service/the_bucket/file.txt, 8888", //
            "bfs://127.0.0.1:123/s/b/foo/bar/baz, 123" //
    })
    @ParameterizedTest
    void testGetPort(final String inputUri, final int expectedPath) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).getPort(), equalTo(expectedPath));
    }

    @Test
    void testGetDefaultPortWithoutTls() {
        assertThat(createRandomBucketFsUrl(false).getDefaultPort(), equalTo(2580));
    }

    private BucketFsUrl createRandomBucketFsUrl(final boolean useTls) {
        try {
            return new BucketFsUrl("foo", 1234, "service", "bucket", "/path", useTls);
        } catch (final MalformedURLException exception) {
            throw new AssertionError("Unable to create BucketFS URL required for tests", exception);
        }
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
        assertThat(BucketFsUrl.create(inputUri).getHost(), equalTo(expectedHost));
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

    @CsvSource({ //
            "bfs://a/b/c/, false", //
            "bfss://a/b/c/, true" //
    })
    @ParameterizedTest
    void testIsTlsEnabled(final String inputUri, final boolean useTls) throws MalformedURLException {
        assertThat(BucketFsUrl.create(inputUri).isTlsEnabled(), equalTo(useTls));
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

    @Test
    void testEqualsContract() {
        EqualsVerifier.forClass(BucketFsUrl.class)
                .withIgnoredFields("cachedServiceName", "cachedBucketName", "cachedPathInBucket").verify();
    }
}