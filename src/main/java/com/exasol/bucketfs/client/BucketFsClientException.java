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
    public BucketFsClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Wrap a {@link BucketAccessException}.
     * <p>
     * In most cases a {@link BucketAccessException} already contains everything the user needs to know, so the only
     * thing left to do is wrap them in the right exception type so that the BucketFS client can use them to report the
     * error.
     * </p>
     *
     * @param wrappedException actual thing that went wrong
     */
    public BucketFsClientException(final BucketAccessException wrappedException) {
        super(wrappedException.getMessage(), wrappedException);
    }
}