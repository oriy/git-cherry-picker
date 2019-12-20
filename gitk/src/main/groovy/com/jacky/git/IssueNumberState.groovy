package com.jacky.git

import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.SearchIssue

class IssueNumberState {
    final int issueNumber;
    final String issueState;

    IssueNumberState(int issueNumber, String issueState) {
        this.issueNumber = issueNumber
        this.issueState = issueState
    }

    static IssueNumberState fromIssue(Issue issue) {
        new IssueNumberState(issue.number, issue.state)
    }

    static IssueNumberState fromSearchIssue(SearchIssue searchIssue) {
        new IssueNumberState(searchIssue.number, searchIssue.state)
    }
}