package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.MergeStatus
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.PullRequestService

import static org.eclipse.egit.github.core.client.IGitHubConstants.*

/**
 * User: oriy
 * Date: 04/02/2019
 */
class PullRequestMergeService extends PullRequestService {

    public static final String COMMIT_MESSAGE = "commit_message"
    public static final String MERGE_METHOD = "merge_method"

    PullRequestMergeService(GitHubClient client) {
        super(client)
    }

    MergeStatus merge(IRepositoryIdProvider repository, int id,
                      String commitMessage, MergeMethod mergeMethod = MergeMethod.MERGE) throws IOException {
        String repoId = getId(repository)
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS)
        uri.append('/').append(repoId)
        uri.append(SEGMENT_PULLS)
        uri.append('/').append(id)
        uri.append(SEGMENT_MERGE)

        Map<String, String> params = new HashMap<String, String>(2, 1)
        params.put(COMMIT_MESSAGE, commitMessage)
        params.put(MERGE_METHOD, mergeMethod.name().toLowerCase())

        return client.put(uri.toString(), params, MergeStatus.class)
    }

    static enum MergeMethod {
        MERGE,
        SQUASH,
        REBASE
    }
}
