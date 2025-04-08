package com.exasol.bucketfs.native_image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.ProcessExecutor;
import com.exasol.bucketfs.url.OsCheck;
import com.exasol.bucketfs.url.OsCheck.OSType;

class NativeImageIT {
    @Test
    void testNativeImage() throws IOException, InterruptedException {
        final String suffix = new OsCheck().getOperatingSystemType() == OSType.WINDOWS ? ".exe" : "";
        final ProcessExecutor executor = new ProcessExecutor("bfsc" + suffix).run("--help");
        executor.assertProcessFinishes();
        assertThat(executor.getStdOut(), startsWith("Usage: "));
    }
}
