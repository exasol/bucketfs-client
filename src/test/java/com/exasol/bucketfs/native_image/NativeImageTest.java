package com.exasol.bucketfs.native_image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.JarExecutor;

@Tag("native-image")
class NativeImageTest {

    @Test
    void testNativeImage() throws IOException, InterruptedException {
        final JarExecutor executor = new JarExecutor().run("--help");
        executor.assertProcessFinishes();
        assertThat(executor.getStdOut(), startsWith("Usage: "));
    }
}