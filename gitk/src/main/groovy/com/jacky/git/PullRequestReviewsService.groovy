package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GitHubService

import static org.eclipse.egit.github.core.client.IGitHubConstants.*

/**
 * User: oriy
 * Date: 04/02/2019
 */
class PullRequestReviewsService extends GitHubService {

    public static final String SEGMENT_REVIEWS = "/reviews"
    public static final String BODY = "body"
    public static final String EVENT = "event"
    public static final String APPROVE = "APPROVE"

    List<GitHubClient> clients

    public PullRequestReviewsService(List<GitHubClient> clients) {
        super(clients.first())
        this.clients = clients
    }

    public void approvePullRequest(IRepositoryIdProvider repositoryId, int prNumber) {

        // https://developer.github.com/v3/pulls/reviews/#create-a-pull-request-review
        // POST /repos/:owner/:repo/pulls/:number/reviews

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS)
                .append('/').append(repositoryId.generateId())
                .append(SEGMENT_PULLS)
                .append('/').append(prNumber)
                .append(SEGMENT_REVIEWS)

        Map<String, String> params = new HashMap<String, String>(2, 1)
        params.put(BODY, ':cherries: approved')
        params.put(EVENT, APPROVE)

        for (GitHubClient client: clients) {
            try {
                client.post(uri.toString(), params, PullRequest.class)
            } catch(Exception e) {
                println("PR approval failed with exception: ${e.message}")
            }
        }
    }
}
