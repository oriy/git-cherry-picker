package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.SearchIssue
import org.eclipse.egit.github.core.service.IssueService

import static org.eclipse.egit.github.core.service.IssueService.*

/**
 * User: oriy
 * Date: 24/05/2017
 */
class IssueServiceHelper {

    public static List<Issue> getOpenIssuesByLabel(IssueService issueService, IRepositoryIdProvider repositoryId, String label) {
        Map<String, String> filter = new HashMap<>()
        filter.put(FILTER_STATE, STATE_OPEN)
        filter.put(FILTER_LABELS, label)
        return issueService.getIssues(repositoryId, filter)
    }

    public static List<SearchIssue> getOpenIssuesByQuery(IssueService issueService, IRepositoryIdProvider repositoryId, String title) {
        issueService.searchIssues(repositoryId, STATE_OPEN, "\"${title}\" is:issue is:open".toString())
    }
}
