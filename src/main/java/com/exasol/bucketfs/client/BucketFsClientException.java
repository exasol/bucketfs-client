package com.exasol.bucketfs.client;

import com.exasol.bucketfs.BucketAccessException;

/**
 * Wrapper exception for problems that cannot be mitigated in software and need to be addressed to the user.
 *
 * @serial
 */
public class BucketFsClientException extends RuntimeException {
    private static final long serialVersionUID = -1794748323307653561L;

    /**
     * Create a new {@link BucketFsClientException}.
     *
     * @param message error message
     */
    public BucketFsClientException(final String message) {
        super(message);
    }

    /**
     * Create a new {@link BucketFsClientException}.
     *
     * @param message error message
     * @param cause   exception that caused this one
     */
    public BucketFsClientException(final String message, final BucketAccessException cause) {
        super(message, cause);
    }
}