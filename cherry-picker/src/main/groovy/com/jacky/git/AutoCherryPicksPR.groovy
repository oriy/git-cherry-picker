package com.jacky.git

import com.jacky.GmailSender
import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.IssueService
import org.eclipse.egit.github.core.service.LabelService
import org.eclipse.egit.github.core.service.PullRequestService

import java.util.regex.Pattern

import static com.jacky.git.GitHubUtil.CHERRY_PICK_BODY
import static com.jacky.git.GitHubUtil.CHERRY_PICK_TITLE
import static com.jacky.git.GitHubUtil.REPOSITORIES_DIR
import static com.jacky.git.GitHubUtil.getGitHubIssueUrl
import static com.jacky.git.IssueServiceHelper.getOpenIssuesByQuery
import static com.jacky.git.ReportPrinter.printHtml
import static com.jacky.git.ReportPrinter.printHtmlContent
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN

/**
 * ToTally VictOrious Hackathon 2016
 *
 * Tally Bonfil-Gabay
 * Victor Bronstein
 * Ori Yechieli
 *
 */
class AutoCherryPicksPR {

    private static final String REPORT_NAME = 'Auto Cherry Pick Report'
    private static final String LABEL_NAME = 'cherry-pick'

    private static def doNotCherryPickMessages = [
            "no-cherry-pick",
            "no cherry pick", "no cherrypick", "no cherry-pick",
            "don't cherry pick", "don't cherrypick", "don't cherry-pick",
            "do not cherry pick", "do not cherrypick", "do not cherry-pick"
    ]
    private static final String EMAIL_HEADER = '<body>'
    private static final String EMAIL_FOOTER = '</ul><br><b>Please go over this report and if your code was not merged, it is YOUR responsibility to fix it!</b><br></body>'

    private AutoCherryPickContext context
    private GitCommandExecutor gitExec
    private GitDiffParser gitDiffParser
    private GitMergeMail gitMergeMail

    public AutoCherryPicksPR(AutoCherryPickContext context) {
        this.context = context
        this.gitExec = new GitCommandExecutor().verbose(context.isVerbose())
        this.gitDiffParser = new GitDiffParser(gitExec)
        this.gitMergeMail = new GitMergeMail(context)
    }

    public static void main(String[] args) {
        AutoCherryPickContext context = AutoCherryPickCommandParser.parseArgs(args)
        if (!context) {
            return
        }

        AutoCherryPicksPR autoCherryPicksPR = new AutoCherryPicksPR(context)
        GitMergeMail gitMergeMail = autoCherryPicksPR.gitMergeMail

        gitMergeMail.appendBody(EMAIL_HEADER)

        boolean shouldSendMail = false
        context.getBranchesMap().each { sourceBranch, targetBranch ->
            gitMergeMail.appendBody """<br>$REPORT_NAME from branch <b>$sourceBranch</b> to branch <b>$targetBranch</b><br><br><ul> """
            shouldSendMail = autoCherryPicksPR.runAutoCherryPicks(context.getRepositoryUrl(), sourceBranch, targetBranch, REPOSITORIES_DIR, false, context.isDryRun()) || shouldSendMail
        }

        if (context.isVerbose()) {
            gitMergeMail.getToAddress().each { it ->
                println(it)
            }
        }

        if (shouldSendMail) {
            println('Sending email')
            println(gitMergeMail.getBody())

            GmailSender gmailSender = new GmailSender()
            gitMergeMail.appendBody EMAIL_FOOTER
            gmailSender.simpleMail(gitMergeMail.toAddress, "$REPORT_NAME for: ${context.getRepoName()}", gitMergeMail.getBody())
        }
        else {
            println('Ended, no email sent')
        }
    }

    GitMergeMail getGitMergeMail() {
        return gitMergeMail
    }

    GitCommandExecutor getGitExec() {
        return gitExec
    }

    boolean runAutoCherryPicks(String url, String sourceBranch, String targetBranch, String dir, boolean discardOutput, boolean dryRun) {
        String localTargetBranch = targetBranch.substring(targetBranch.indexOf('/') + 1)

        gitExec.discardOutput(discardOutput)
        File repoDir = gitExec.gitGetRepository(url, localTargetBranch, dir)
        gitExec.repoDir(repoDir)
        String repoName = context.getRepoName()
        String repoOwner = context.getRepoOwner()
        IRepositoryIdProvider repositoryId = GitHubUtil.createRepositoryId(repoOwner, repoName)

        GitHubUtil.setRepoGitConfig(gitExec)

        GitHubClient gitHubClient = GitHubUtil.createGitHubClient()
        PullRequestService pullRequestService = new PullRequestService(gitHubClient)
        PullRequestReviewsService pullRequestReviewsService = new PullRequestReviewsService(GitHubUtil.createApproverGitHubClients())
        IssueService issueService = new IssueService(gitHubClient)
        LabelService labelService = new LabelService(gitHubClient)

        Label mainLabel = createLabel(labelService, repositoryId, LABEL_NAME)
        String branchLabelName = "$LABEL_NAME-$localTargetBranch"
        Label branchLabel = createLabel(labelService, repositoryId, branchLabelName)
        List<Label> labels = [mainLabel, branchLabel]

        println("Merging green cherry-pick PRs for label '$branchLabelName'")
        PullRequestMergeResult pullRequestMergeResult
        if (!dryRun) {
            GitGreenMerger greenMerger = new GitGreenMerger(gitExec, gitHubClient, repositoryId)
            pullRequestMergeResult = greenMerger.mergeAllMergable(branchLabelName, false, dryRun)
        }

        def shouldPushMaster = false

        List<CherryPicksResult> result = gitDiffParser.findCommitsToCherryPick(sourceBranch, targetBranch)

        boolean shouldSendMail = false
        result.each { CherryPicksResult commit ->

            String commitHash = commit.commitHash

            RepositoryCommit repositoryCommit = GitHubUtil.getRepositoryCommit(repositoryId, commitHash)

            CommitDescription commitDescription = new CommitDescription(commit, repositoryCommit)
            String userName = commitDescription.getUserName()
            String userEmail = commitDescription.getUserEmail()

            if (commitDescription.getUser() == null) {
                gitMergeMail.addCommitToReport("<li><font color='#dd0000'> <b>INVALID GITHUB USERNAME</b> ${userName} <b>$userEmail</b></font></li> ", userEmail)
                gitMergeMail.appendBody("<br/><br/>")
            }

            gitExec.gitCheckout(localTargetBranch)

            if (shouldCommitBeCherryPicked(commit)) {

                String branchName = getBranchName(commit)
                if (gitExec.branchExists(branchName)) {
                    return
                    // the branch already exists, PR already exists
                }
                gitExec.gitCreateNewBranch(branchName)

                def processOutput = gitExec.gitCherryPick(commitHash)
                boolean failed = processOutput.failed()
                CommitResult commitResult

                if (failed) {
                    commitResult = CommitResult.CONFLICT
                } else {
                    commitResult = CommitResult.PULL_REQUEST
                    if (!dryRun) {
                        processOutput = gitExec.gitPush(branchName)
                        failed = processOutput.failed()
                        commitResult = failed ? CommitResult.PUSH_ERROR : CommitResult.PULL_REQUEST
                    }
                }

                int issueNumber = 1
                IssueNumberState issueNumberState

                if (failed) {
                    if (!dryRun) {
                        issueNumberState = getOrCreateIssue(gitExec, issueService, repositoryId, commitDescription, commitResult, commitDescription.getUser(), labels)
                        issueNumber = issueNumberState.issueNumber
                    }

                    gitExec.gitResetLastCommit()

                    if (issueNumberState.issueState.equals(STATE_OPEN)) {
                        if (!dryRun) {
                            shouldSendMail = true
                        }
                        gitMergeMail.addCommitToReport(printHtml(commitDescription, commitResult, getGitHubIssueUrl(repositoryId, issueNumber)), userEmail)
                        gitMergeMail.appendBody("<br/><br/>")
                    }
                    return

                } else {
                    if (!dryRun) {
                        Issue issue = createPR(pullRequestService, pullRequestReviewsService, issueService, repositoryId, localTargetBranch, branchName, commit, commitDescription.getUser(), labels)
                        issueNumberState = IssueNumberState.fromIssue(issue)
                        issueNumber = issueNumberState.issueNumber
                    }
                    gitMergeMail.appendBody(printHtml(commitDescription, CommitResult.PULL_REQUEST, getGitHubIssueUrl(repositoryId, issueNumber)))
                }

            } else {
                def processOutput = gitExec.gitCherryPickRecordMerge(commitHash)
                if (processOutput.exitValue == 0) {
                    gitMergeMail.addCommitToReport(printHtml(commitDescription, CommitResult.RECORD_ONLY, ''), userEmail)
                    shouldPushMaster = !dryRun
                } else {
                    CommitResult commitResult = CommitResult.RECORD_ONLY_FAILED
                    int issueNumber = 1

                    if (!dryRun) {
                        issueNumber = getOrCreateIssue(gitExec, issueService, repositoryId, commitDescription, commitResult, commitDescription.getUser(), labels)
                    }
                    gitMergeMail.addCommitToReport(printHtml(commitDescription, commitResult, getGitHubIssueUrl(repositoryId, issueNumber)), userEmail)
                    gitExec.gitResetLastCommit()
                }
                shouldSendMail = !dryRun
            }
            gitMergeMail.appendBody("<br/><br/>")
        }

        // Push master branch (for record-only cherry-picks)
        if (!dryRun && shouldPushMaster) {
            def processOutput = gitExec.gitPush(localTargetBranch)
            if (processOutput.failed()) {
                throw new RuntimeException("Error pushing into ${localTargetBranch} : ${processOutput.output}")
            }
        }

        if (!dryRun && pullRequestMergeResult.hasOpenPullRequests()) {
            gitMergeMail.appendBody """</ul><br><b>Open cherry-pick pull requests</b> from branch <b>$sourceBranch</b> to branch <b>$targetBranch</b><br><br><ul> """
            CommitResult commitResult = CommitResult.PULL_REQUEST_FAILED
            pullRequestMergeResult.getOpenPullRequests().each { PullRequest pullRequest ->
                RepositoryCommit repositoryCommit = GitHubUtil.getRepositoryCommit(repositoryId, pullRequest.getHead().getSha())
                CommitDescription commitDescription = new CommitDescription(pullRequest, repositoryCommit)
                gitMergeMail.addCommitToReport(printHtml(commitDescription, commitResult, getGitHubIssueUrl(repositoryId, pullRequest.getNumber())), commitDescription.getUserEmail())
                gitMergeMail.appendBody("<br/><br/>")
            }
            shouldSendMail = true
        }

        shouldSendMail
    }

    private static Label createLabel(LabelService labelService, IRepositoryIdProvider repositoryId, String labelName) {
        String color = (labelName == LABEL_NAME) ? 'e19191' : 'c40000'
        Label label = new Label().setName(labelName).setColor(color)
        try {
            label = labelService.createLabel(repositoryId, label)
        } catch (RequestException ignore) {
        }
        return label
    }

    private static Issue createPR(PullRequestService pullRequestService, PullRequestReviewsService pullRequestReviewsService,
                                  IssueService issueService, IRepositoryIdProvider repositoryId,
                                  String baseBranch, String headBranch, CherryPicksResult commit, User user, List<Label> labels) {
        String commitHash = commit.commitHash
        PullRequest pr = new PullRequest()
                .setTitle(CHERRY_PICK_TITLE + ' ' + commitHash.substring(0, 7) + ': ' + commit.commitMessage)
                .setBody(CHERRY_PICK_BODY + ' of ' + commitHash)
                .setHead(new PullRequestMarker().setLabel(headBranch))
                .setBase(new PullRequestMarker().setLabel(baseBranch))
        PullRequest prResponse = pullRequestService.createPullRequest(repositoryId, pr)
        int prNumber = prResponse.getNumber()

        Issue issue = issueService.getIssue(repositoryId, prNumber)
        issue.setLabels(labels).setAssignee(user)
        issueService.editIssue(repositoryId, issue)

        pullRequestReviewsService.approvePullRequest(repositoryId, prNumber)

        issue
    }

    private static IssueNumberState getOrCreateIssue(GitCommandExecutor gitExec, IssueService issueService, IRepositoryIdProvider repositoryId,
                                                     CommitDescription commit, CommitResult commitResult, User user, List<Label> labels) {
        String commitHash = commit.commitHash
        String title = 'FAILED ' + CHERRY_PICK_TITLE + ' ' + commitHash.substring(0, 7)
        List<SearchIssue> issueList = getOpenIssuesByQuery(issueService, repositoryId, title)

        if (!issueList.isEmpty()) {
            return IssueNumberState.fromSearchIssue(issueList.get(0))
        }

        IssueNumberState.fromIssue(createIssue(gitExec, issueService, repositoryId, commit, commitResult, user, labels, commitHash, title))
    }

    private static Issue createIssue(GitCommandExecutor gitExec, IssueService issueService, IRepositoryIdProvider repositoryId,
                                     CommitDescription commit, CommitResult commitResult, User user, List<Label> labels, String commitHash, String title) {
        boolean recordOnlyFailed = (commitResult == CommitResult.RECORD_ONLY_FAILED)

        GitCommand gitPushCommand = gitExec.gitPushCommand(getBranchName(commitHash))
        StringBuilder bodySb = new StringBuilder()
        bodySb.append('FAILED ' + CHERRY_PICK_BODY + ' of ').append(commitHash).append('\n\n')
        bodySb.append(printHtmlContent(commit, commitResult)).append('\n\n')

        if (!recordOnlyFailed) {
            List<GitCommand> gitCommands = getCherryPickCommandsFor(gitExec, commitHash, [gitExec.gitCherryPickCommand(commitHash)])
            StringBuilder cherryPickBlockSb = new StringBuilder()
            cherryPickBlockSb.append(gitCommandsBlock(gitCommands))
            cherryPickBlockSb.append('<pre>once you resolve conflicts, commit all changes</br>and finally:</pre>\n')
            cherryPickBlockSb.append(gitCommandsBlock([gitPushCommand]))
            appendDetailsBlock(bodySb, ' For <b>manual :cherries: pick</b> of your commit, use these commands:', cherryPickBlockSb.toString())
            bodySb.append('\n\n')
        }
        List<GitCommand> gitCommands = getCherryPickCommandsFor(gitExec, commitHash, gitExec.gitCherryPickRecordMergeCommands(commitHash))
        gitCommands.add(gitPushCommand)
        String commandsBlock = gitCommandsBlock(gitCommands)
        appendDetailsBlock(bodySb, ' Is the commit <b>already :cherries: picked</b>? &nbsp; Would you like to <b>SKIP cherry-pick</b>? &nbsp; use these commands:', commandsBlock)

        Issue issue = new Issue()
                .setTitle(title)
                .setBody(bodySb.toString())
                .setLabels(labels).setAssignee(user)
        issue = issueService.createIssue(repositoryId, issue)
        issue
    }

    static List<GitCommand> getCherryPickCommandsFor(GitCommandExecutor gitExec, String commitHash, List<GitCommand> gitCommands) {
        String branchName = getBranchName(commitHash)
        List<GitCommand> allGitCommands = new ArrayList<>()
        allGitCommands.add(gitExec.gitFetchCommand())
        allGitCommands.add(gitExec.gitCreateNewBranchCommand(branchName))
        allGitCommands.addAll(gitCommands)
        allGitCommands
    }

    private static void appendDetailsBlock(StringBuilder sb, String summary, String details) {
        sb.append('<details>\n')
        sb.append('<summary>').append(summary).append('</summary>\n\n')
        sb.append(details)
        sb.append('\n</details>\n')
    }

    private static String gitCommandsBlock(List<GitCommand> gitCommands) {
        StringBuilder sb = new StringBuilder()
        sb.append('\n```\n')
        gitCommands.each { gitCommand -> sb.append("${gitCommand.getCommand()}\n") }
        sb.append('```\n')
        sb.toString()
    }

    private static String getBranchName(String commitHash) {
        return "cp_$commitHash"
    }

    private static String getBranchName(CherryPicksResult commit) {
        return getBranchName(commit.commitHash)
    }

    static boolean shouldCommitBeCherryPicked(CherryPicksResult commit) {
        def result = true
        def msg = commit.commitMessage.toLowerCase()
        doNotCherryPickMessages.each {
            if (msg.contains(it)) {
                result = false
            }
        }
        return result;
    }

}
