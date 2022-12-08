package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.bucketfs.url.OsCheck;
import com.exasol.bucketfs.url.OsCheck.OSType;

class OsCheckTest {
    private static final String OS_NAME_SYSTEM_PROPERTY = "os.name";
    private OsCheck osCheck;

    @BeforeEach
    void setup() {
        this.osCheck = new OsCheck();
    }

    @Test
    void getOperatingSystemType() {
        assertThat(this.osCheck.getOperatingSystemType(), allOf(notNullValue(), not(equalTo(OSType.OTHER))));
    }

    @ParameterizedTest
    @CsvSource(nullValues = { "NULL" }, value = { "Mac OS X, MACOS", "MAC OS X, MACOS", "Mac, MACOS", "_Mac_, MACOS",
            "Darwin, MACOS", "Windows 10, WINDOWS", "Windows 11, WINDOWS", "Win, WINDOWS", "_Win_, WINDOWS",
            "linux, LINUX", "LINUX DEBIAN, LINUX", "unix, OTHER", "NULL, OTHER" })
    void detectOperatingSystemType(final String osNameSystemProperty, final OSType expectedType) {
        final String orgValue = System.getProperty(OS_NAME_SYSTEM_PROPERTY);
        try {
            setSystemProperty(osNameSystemProperty);
            assertThat(this.osCheck.getOperatingSystemType(), equalTo(expectedType));
        } finally {
            setSystemProperty(orgValue);
        }
    }

    private void setSystemProperty(final String value) {
        if (value == null) {
            System.clearProperty(OS_NAME_SYSTEM_PROPERTY);
        } else {
            System.setProperty(OS_NAME_SYSTEM_PROPERTY, value);
        }
    }
}