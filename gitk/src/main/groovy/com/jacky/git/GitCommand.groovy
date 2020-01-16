package com.jacky.git

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitCommand {
    String command
    File repoDir
    int maxAttempts = 1
    boolean retryOnEmptyResponse
    boolean discardOutput

    public GitCommand(String command) {
        this.command = command
    }

    public GitCommand maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts
        return this
    }

    public GitCommand retryOnEmptyResponse(boolean retryOnEmptyResponse) {
        this.retryOnEmptyResponse = retryOnEmptyResponse
        return this
    }

    public GitCommand discardOutput(boolean discardOutput) {
        this.discardOutput = discardOutput
        return this
    }

    public GitCommand repoDir(File repoDir) {
        this.repoDir = repoDir
        return this
    }
}
