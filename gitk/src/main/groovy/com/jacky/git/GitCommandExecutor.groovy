package com.jacky.git

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitCommandExecutor {
    boolean verbose = false
    PrintStream printOut = System.out
    File repoDir
    boolean discardOutput

    GitCommandExecutor repoDir(File repoDir) {
        this.repoDir = repoDir
        return this
    }

    GitCommandExecutor discardOutput(boolean discardOutput) {
        this.discardOutput = discardOutput
        return this
    }

    GitCommandExecutor verbose(boolean verbose) {
        this.verbose = verbose
        return this
    }

    GitCommand gitCommand(String command) {
        gitCommand(command, discardOutput, repoDir)
    }

    GitCommand gitCommand(String command, boolean discardOutput) {
        gitCommand(command, discardOutput, repoDir)
    }

    static GitCommand gitCommand(String command, boolean discardOutput, File repoDir) {
        new GitCommand(command)
                .discardOutput(discardOutput)
                .repoDir(repoDir)
    }

    public GitCommandResult execute(GitCommand gitCommand, boolean discardOutput) {
        execute gitCommand.discardOutput(discardOutput)
    }

    public GitCommandResult execute(GitCommand gitCommand, File repoDir) {
        execute gitCommand.repoDir(repoDir)
    }

    public GitCommandResult execute(GitCommand gitCommand, boolean discardOutput, File repoDir) {
        execute(gitCommand.discardOutput(discardOutput), repoDir)
    }

    public GitCommandResult execute(GitCommand gitCommand) {
        def commandToExecute = gitCommand.getCommand()
        if (verbose) {
            String stringCommandToExecute = commandToExecute.replaceAll("(//.*):.*@", "\$1@")
            printOut.println '[DEBUG] executing gitCommand \'' + stringCommandToExecute + '\''
        }

        int maxAttempts = gitCommand.maxAttempts
        int attempt = 1

        String output = ''
        String error = ''
        int exitValue = 0
        boolean retry = true

        while (retry) {

            def process = commandToExecute.execute((String[]) null, gitCommand.getRepoDir())

            def outBuffer = new ByteArrayOutputStream()
            def errBuffer = new ByteArrayOutputStream()

            if (gitCommand.isDiscardOutput()) {
                if (verbose) {
                    process.consumeProcessOutput((Appendable) printOut, printOut)
                } else {
                    process.consumeProcessOutput(outBuffer, errBuffer)
                }
            } else {
                process.consumeProcessOutput(outBuffer, errBuffer)
            }

            process.waitFor()

            output = outBuffer.toString()
            error = errBuffer.toString()
            exitValue = process.exitValue()

            if (verbose) {
                if (!gitCommand.isDiscardOutput()) {
                    printOut.println output
                    printOut.println error

                }
                printOut.println '[DEBUG] exit value : ' + exitValue \
                                + ((attempt > 1) ? ", attempt #$attempt" : '') \
                                + '\n'
            }

            if (gitCommand.retryOnEmptyResponse) {
                retry = GitCommandResult.isEmptyResponse(output)
            }

            retry &= (++attempt <= maxAttempts)
            if (retry) {
                sleep(1000)
            }
        }

        return new GitCommandResult(exitValue: exitValue, output: output, error: error)
    }

    public File gitGetRepository(String repositoryUrl, String branchName, String checkoutPath) {
        String projectName = GitHubUtil.getProjectNameFromUrl(repositoryUrl)
        File checkoutDir = new File(checkoutPath)
        File repoDir = new File(checkoutPath + '/' + projectName)
        if (repoDir.exists() && new File(repoDir, '.git').exists()) {
            execute(gitCheckoutCommand(branchName), repoDir)
            execute(gitFetchCommand("--all --prune"), discardOutput, repoDir)
            execute(gitPullCommand(), discardOutput, repoDir)
        } else {
            execute(gitCloneCommand(repositoryUrl), discardOutput, checkoutDir)
            execute(gitCheckoutCommand(branchName), repoDir)
        }
        return repoDir
    }

    public GitCommand gitCloneCommand(String repositoryOriginUrl) {
        gitCommand("git clone $repositoryOriginUrl")
    }

    public GitCommandResult gitClone(String repositoryOriginUrl) {
        execute(gitCloneCommand(repositoryOriginUrl))
    }

    public GitCommand gitLogCommand(String branchName) {
        gitCommand("git log $branchName --pretty=format:|~entry~||~author~|%an|~!author~||~commit_date~|%cd|~!commit_date~||~message_body~|%b|~!message_body~||~message_subject~|%s|~!message_subject~||~!entry~|\n")
    }

    public GitCommandResult gitLog(String branchName, int maxAttempts = 1) {
        execute(gitLogCommand(branchName).maxAttempts(maxAttempts).retryOnEmptyResponse(maxAttempts > 1))
    }

    public GitCommand gitFetchCommand(String flags = '') {
        gitCommand("git fetch $flags")
    }

    public GitCommand gitPullCommand(String flags = '') {
        gitCommand("git pull $flags")
    }

    public GitCommandResult gitPull() {
        gitPull('')
    }

    public GitCommandResult gitPull(String flags) {
        execute(gitPullCommand(flags))
    }

    public GitCommand gitPushCommand(String branchName) {
        gitCommand("git push origin $branchName")
    }

    public GitCommandResult gitPush(String branchName) {
        execute(gitPushCommand(branchName).discardOutput(false))
    }

    public GitCommand gitCheckoutCommand(String branchName) {
        gitCommand("git checkout $branchName")
    }

    public GitCommandResult gitCheckout(String branchName) {
        execute(gitCheckoutCommand(branchName), false)
    }

    public GitCommand gitCreateNewBranchCommand(String branchName) {
        gitCommand("git checkout -b $branchName")
    }

    public GitCommandResult gitCreateNewBranch(String branchName) {
        execute(gitCreateNewBranchCommand(branchName))
    }

    public GitCommand gitConfigLocalCommand(String key, String value) {
        gitCommand("git config --local ${key} \"${value}\"")
    }

    public GitCommandResult gitConfigLocal(String key, String value) {
        execute(gitConfigLocalCommand(key, value))
    }

    public GitCommand gitCherryDiffCommand(String targetBranch, String sourceBranch) {
        gitCommand("git cherry -v $targetBranch $sourceBranch")
    }

    public GitCommandResult gitCherryDiff(String targetBranch, String sourceBranch, int maxAttempts = 1) {
        execute(gitCherryDiffCommand(targetBranch, sourceBranch).maxAttempts(maxAttempts).retryOnEmptyResponse(maxAttempts > 1))
    }

    public GitCommand gitShowRemoteBranchCommand(String branchName) {
        gitCommand("git show origin/${branchName}")
    }

    public GitCommandResult gitShowRemoteBranch(String branchName) {
        execute(gitShowRemoteBranchCommand(branchName), true)
    }

    public boolean branchExists(String branchName) {
        gitShowRemoteBranch(branchName).succeeded()
    }

    public GitCommand gitDeleteRemoteBranchCommand(String branchNameWithoutPrefix) {
        gitCommand('git push origin --delete ' + branchNameWithoutPrefix)
    }

    public GitCommandResult gitDeleteRemoteBranch(String branchNameWithoutPrefix) {
        execute(gitDeleteRemoteBranchCommand(branchNameWithoutPrefix), false)
    }

    public GitCommand gitCherryPickCommand(String commitHash) {
        gitCommand("git cherry-pick -x $commitHash")
    }

    public GitCommandResult gitCherryPick(String commitHash) {
        execute(gitCherryPickCommand(commitHash))
    }

    public List<GitCommand> gitCherryPickRecordMergeCommands(String commitHash) {
        [ gitCommand("git cherry-pick -x --strategy=ours $commitHash"),
          gitCommand('git commit --allow-empty --no-edit') ]
    }

    public GitCommandResult gitCherryPickRecordMerge(String commitHash) {
        GitCommandResult gitCommandResult = null
        gitCherryPickRecordMergeCommands(commitHash).each { gitCommand ->
            gitCommandResult = execute(gitCommand)
        }
        gitCommandResult
    }

    public GitCommand gitResetLastCommitCommand() {
        gitCommand("git reset --hard HEAD")
    }

    public GitCommandResult gitResetLastCommit() {
        execute(gitResetLastCommitCommand())
    }

    public String gitShowCommitAuthorEmail(String commitHash) {
        GitCommand gitShowUserEmailCommand = gitCommand("git show ${commitHash} --format=\"%ae\" --no-patch").discardOutput(false)
        execute(gitShowUserEmailCommand).getOutput().toLowerCase()
    }

}
