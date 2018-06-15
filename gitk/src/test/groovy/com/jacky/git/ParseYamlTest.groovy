package com.jacky.git

import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * User: oriy
 * Date: 02/02/2017
 */
class ParseYamlTest {

    @Test
    public void testParse() throws Exception {
        File file = new File(this.getClass().getResource("/config.yml").getFile());
        Configuration configuration = Configuration.parseYaml(file)

        assertEquals(GitHubUtil.THIS_REPOSITORY, configuration.getRepository())
        assertEquals('org', configuration.getOrganization())
        assertEquals('user', configuration.getGitUserName())
        assertEquals('user@org.com', configuration.getGitUserEmail())
        assertEquals('pass', configuration.getGitUserPass())
        assertEquals('1234token', configuration.getGitUserToken())
        assertEquals('gmailuser', configuration.getGmailUser())
        assertEquals('gmailpass', configuration.getGmailPass())
    }
}
