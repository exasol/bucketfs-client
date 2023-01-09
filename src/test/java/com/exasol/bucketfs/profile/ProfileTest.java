package com.exasol.bucketfs.profile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.exasol.bucketfs.url.BucketFsUrl;

import nl.jqno.equalsverifier.EqualsVerifier;

class ProfileTest {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void emptyProfile(final boolean decode) {
        final Profile testee = Profile.empty(decode);
        assertThat(testee.host(), nullValue());
        assertThat(testee.port(), equalTo(BucketFsUrl.UNDEFINED_PORT));
        assertThat(testee.bucket(), nullValue());
        assertThat(testee.readPassword(), nullValue());
        assertThat(testee.writePassword(), nullValue());
        assertThat(testee.decodePasswords(), is(decode));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.simple().forClass(Profile.class).verify();
    }
}
