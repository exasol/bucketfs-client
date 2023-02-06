package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class VersionFromManifestTest {
    @Test
    void testSuccess() throws Exception {
        final VersionFromManifest testee = new VersionFromManifest("/MANIFEST.MF");
        assertThat(testee.getVersion()[0], equalTo("9.8.7"));
    }

    @Test
    void testNoManifest() throws Exception {
        final VersionFromManifest testee = new VersionFromManifest("/non-existing-file");
        assertNull(testee.getVersion()[0]);
    }

    @Test
    void testDefaultConstructor() throws Exception {
        final VersionFromManifest testee = new VersionFromManifest();
        assertThat(testee.getResourcePath(), equalTo(VersionFromManifest.DEFAULT_MANIFEST_PATH));
    }
}
