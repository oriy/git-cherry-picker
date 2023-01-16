package com.jacky.git

/**
 * User: oriy
 * Date: 24/05/2017
 */
class ReportPrinter {

    static GString printHtml(CommitDescription commit, CommitResult commitResult, String url = '') {
        "<li><font color='#${htmlColor(commitResult)}'>${printHtmlContent(commit, commitResult, url)}</font></li>\n"
    }

    static GString printHtmlContent(CommitDescription commit, CommitResult commitResult, String url = '') {
        " ${htmlResult(commitResult, url)}" +
        " commit: <b>${commit.getCommitHash()}</b><br/>" +
        " User: <b>${commit.getUserName()}</b>, Author: ${commit.getCommitUserName()} <b>${commit.getUserEmail()}</b>;<br/>" +
        " Message: <b>$commit.commitMessage</b>;" +
        (commitResult.isError() ? "<br/><b>Please handle manually!</b>" : "")
    }

    private static String htmlResult(CommitResult commitResult, String url) {
        switch (commitResult) {
            case CommitResult.PULL_REQUEST:
                return "<b><a href='${url}'>CREATED PR</a> SUCCESSFULLY</b>:"
            case CommitResult.PULL_REQUEST_FAILED:
                return "<b><a href='${url}'>CHERRY-PICK PR</a> FAILED</b>:"
            case CommitResult.RECORD_ONLY:
                return '<b>RECORD ONLY (SKIPPED CHERRY-PICK)</b>'
            case CommitResult.CONFLICT:
                return "<b><a href='${url}'>CREATED ISSUE</a> - CONFLICT</b> merging"
            case CommitResult.RECORD_ONLY_FAILED:
                return "<b><a href='${url}'>CREATED ISSUE</a> - RECORD ONLY FAILED</b>"
            case CommitResult.PUSH_ERROR:
                return "<b><a href='${url}'>CREATED ISSUE</a> - ERROR</b> pushing branch"
        }
    }

    private static String htmlColor(CommitResult commitResult) {
        switch (commitResult) {
            case CommitResult.PULL_REQUEST:
                return '22bb00'
            case CommitResult.RECORD_ONLY:
                return '00aaaa'
            default:
                return 'dd0000'
        }
    }
}
