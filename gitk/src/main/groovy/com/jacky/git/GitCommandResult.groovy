package com.jacky.git

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitCommandResult {
    int exitValue
    String output
    String error

    boolean succeeded() {
        exitValue == 0
    }

    boolean failed() {
        !succeeded()
    }
}
