package com.jacky.git

/**
 * User: oriy
 * Date: 24/05/2017
 */
enum CommitResult {
    CONFLICT(true),
    PUSH_ERROR(true),
    PULL_REQUEST,
    PULL_REQUEST_FAILED(true),
    RECORD_ONLY,
    RECORD_ONLY_FAILED(true);

    final boolean error
    CommitResult(boolean error) {
        this.error = error
    }
    CommitResult() {
        this(false)
    }
}