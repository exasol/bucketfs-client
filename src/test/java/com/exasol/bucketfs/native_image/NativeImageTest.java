package com.exasol.bucketfs.native_image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.JarExecutor;

@Tag("native-image")
class NativeImageTest {

    private static final Logger LOGGER = Logger.getLogger(NativeImageTest.class.getName());

    @Test
    void testNativeImage() throws IOException, InterruptedException {
        LOGGER.fine("System property native.binary=" + System.getProperty("native.binary"));
        final JarExecutor executor = new JarExecutor().run("--help");
        executor.assertProcessFinishes();
        assertThat(executor.getStdOut(), startsWith("Usage: "));
    }
}