package com.exasol.bucketfs.profile;

import com.exasol.bucketfs.url.BucketFsUrl;

/**
 * Provides a profile with default values for parts of {@link BucketFsUrl} and password.
 */
public interface ProfileProvider {
    /**
     * @return default {@link Profile} with default values for parts of {@link BucketFsUrl} and password.
     */
    default Profile getProfile() {
        return getProfile("default");
    }

    /**
     * @param profileName name of the profile
     * @return {@link Profile} with default values for parts of {@link BucketFsUrl} and password.
     */
    Profile getProfile(String profileName);
}
