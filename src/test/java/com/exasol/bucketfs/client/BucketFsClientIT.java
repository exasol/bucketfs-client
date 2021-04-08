package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.bucketfs.*;
import com.exasol.config.BucketConfiguration;
import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolService;

@Testcontainers
class BucketFsClientIT {
    @Container
    private static ExasolContainer<? extends ExasolContainer<?>> EXASOL = new ExasolContainer<>()//
            .withRequiredServices(ExasolService.BUCKETFS) //
            .withReuse(true);

    @Test
    void testCopyFileFromBucketToLocalFile(@TempDir final Path tempDir)
            throws InterruptedException, BucketAccessException, TimeoutException, IOException {
        final String expectedContent = "the content";
        final String filename = "dir_test.txt";
        final Path destinationFile = tempDir.resolve(filename);
        final String destination = "file://" + destinationFile;
        uploadStringContent(expectedContent, filename);
        final String source = getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
        BucketFsClient.main(new String[] { "cp", source, destination });
        assertThat(Files.readString(destinationFile), equalTo(expectedContent));
    }

    private String getBucketFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return "bfs://" + getHost() + ":" + getMappedBucketFsPort() + "/" + serviceName + "/" + bucketName + "/"
                + pathInBucket;
    }

    private void uploadStringContent(final String content, final String pathInBucket)
            throws InterruptedException, BucketAccessException, TimeoutException {
        getDefaultBucket().uploadStringContentNonBlocking(content, pathInBucket);
        Thread.sleep(500);
    }

    private UnsynchronizedBucket getDefaultBucket() {
        final BucketConfiguration bucketConfiguration = EXASOL.getClusterConfiguration() //
                .getBucketFsServiceConfiguration("bfsdefault") //
                .getBucketConfiguration("default");
        return WriteEnabledBucket.builder() //
                .ipAddress(getHost()) //
                .httpPort(getMappedBucketFsPort()) //
                .serviceName("bfsdefault") //
                .name("default") //
                .writePassword(bucketConfiguration.getWritePassword()) //
                .build();
    }

    private Integer getMappedBucketFsPort() {
        return EXASOL.getMappedPort(EXASOL.getDefaultInternalBucketfsPort());
    }

    private String getHost() {
        return "127.0.0.1";
    }

    @Test
    void testCopyFileFromBucketToFileWithoutProtocol(@TempDir final Path tempDir)
            throws InterruptedException, BucketAccessException, TimeoutException, IOException {
        final String expectedContent = "the content";
        final String filename = "dir_test.txt";
        final Path destinationFile = tempDir.resolve(filename);
        uploadStringContent(expectedContent, filename);
        final String source = getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
        BucketFsClient.main(new String[] { "cp", source, destinationFile.toString() });
        assertThat(Files.readString(destinationFile), equalTo(expectedContent));
    }
}