package com.exasol.bucketfs.native_image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.logging.Logger;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.ProcessExecutor;

@Tag("native-image")
class NativeImageTest {

    private static final Logger LOGGER = Logger.getLogger(NativeImageTest.class.getName());

    @Test
    void testNativeImage() throws IOException, InterruptedException {
        final String binary = System.getProperty("native.binary");
        LOGGER.fine("System property native.binary=" + binary);
        final ProcessExecutor executor = new ProcessExecutor(binary).run("--help");
        executor.assertProcessFinishes();
        assertThat(executor.getStdOut(), startsWith("Usage: "));
    }
}