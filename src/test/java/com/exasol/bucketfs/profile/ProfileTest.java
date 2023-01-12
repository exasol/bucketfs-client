package com.exasol.bucketfs.profile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.url.BucketFsUrl;

import nl.jqno.equalsverifier.EqualsVerifier;

class ProfileTest {

    @Test
    void emptyProfile() {
        final Profile testee = Profile.empty();
        assertThat(testee.host(), nullValue());
        assertThat(testee.port(), equalTo(BucketFsUrl.UNDEFINED_PORT));
        assertThat(testee.bucket(), nullValue());
        assertThat(testee.getReadPassword(), nullValue());
        assertThat(testee.getWritePassword(), nullValue());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.simple().forClass(Profile.class).verify();
    }
}
