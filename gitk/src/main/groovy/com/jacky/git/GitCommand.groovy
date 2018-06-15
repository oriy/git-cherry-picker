package com.jacky.git

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitCommand {
    String command
    File repoDir
    boolean discardOutput

    public GitCommand(String command) {
        this.command = command
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
