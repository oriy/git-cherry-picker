package com.jacky.git

import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * User: oriy
 * Date: 02/02/2017
 */
class ConfigurationUtilTest {

    @Test
    public void testParse() throws Exception {
        File file = new File(this.getClass().getResource("/test_config.yml").getFile());
        Configuration configuration = ConfigurationUtil.parseYaml(file.newInputStream())

        assertEquals(GitHubUtil.THIS_REPOSITORY, configuration.getRepository())
        assertEquals('org', configuration.getOrganization())
        assertEquals('user', configuration.getGitUserName())
        assertEquals('user@org.com', configuration.getGitUserEmail())
        assertEquals('cITmQYaAWR8', configuration.getGitUserPass())
        assertEquals('gmailuser', configuration.getGmailUser())
        assertEquals('LCevAcQ_jOIICs7XUhsm-A', configuration.getGmailPass())
        assertEquals(['zAKontFh5gw', 'oIOI9yoPqVA'], configuration.getApproverTokens())
    }
}
