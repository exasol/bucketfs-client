package com.exasol.bucketfs.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import picocli.CommandLine.IVersionProvider;

public class VersionFromManifest implements IVersionProvider {

    @SuppressWarnings("java:S1075") // this is not a URL, but the path of a resource in BFSC's jar file
    private static final String DEFAULT_MANIFEST_PATH = "/META-INF/MANIFEST.MF";
    private static final String VERSION_ATTRIBUTE = "Implementation-Version";
    private final String resourcePath;

    public VersionFromManifest() {
        this(DEFAULT_MANIFEST_PATH);
    }

    VersionFromManifest(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public String[] getVersion() throws Exception {
        return new String[] { getVersionFromManifest() };
    }

    public String getVersionFromManifest() throws IOException {
        try (InputStream s = VersionFromManifest.class.getResourceAsStream(this.resourcePath)) {
            if (s == null) {
                return null;
            }
            final Manifest manifest = new Manifest(s);
            final Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue(VERSION_ATTRIBUTE);
        }
    }
}
