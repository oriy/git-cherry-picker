package com.jacky.git
/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class AutoCherryPickContext {
    String repositoryUrl
    String repoOwner
    String repoName
    Map<String, String> branchesMap
    String[] emailList
    boolean dryRun
    boolean verbose

    public AutoCherryPickContext(String repoOwner, String repoName, String branches, String emails, boolean dryRun, boolean verbose) {
        this.repositoryUrl = GitHubUtil.getRepoUrl(repoOwner, repoName)
        this.repoOwner = repoOwner
        this.repoName = repoName
        this.branchesMap = GitHubUtil.createMapFromStringArg(branches)
        this.emailList = emails.split(',')
        this.dryRun = dryRun
        this.verbose = verbose
    }
}
