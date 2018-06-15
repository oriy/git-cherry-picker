package com.jacky.git

import org.junit.Test

/**
 * Created by
 * User: Oriy
 * Date: 10/07/2016.
 */
class GitDiffParserTest {

    GitCommandExecutor gitCommandExecutor = new GitCommandExecutor()
    GitDiffParser gitDiffParser = new GitDiffParser(gitCommandExecutor)

    @Test
    void convertLogToXmlTest() {
        def resultXml = gitDiffParser.convertLogToXml('|~entry~||~author~|oriy|~!author~||~commit_date~|Mon May 6 08:39:11 2013 +0300|~!commit_date~||~message_body~||~!message_body~||~message_subject~|Failing streaming test fix - Ignoring failed test|~!message_subject~||~!entry~|')
        assert resultXml.toString() ==
                'oriy' +
                'Mon May 6 08:39:11 2013 +0300' +
                'Failing streaming test fix - Ignoring failed test'
    }

    @Test
    void createCherryPicksResultTest() {
        def result = gitDiffParser.createCherryPicksResult(['+ 583c20a21bdb1c9648ed75a6712678c730896882 this is a. test! of commit message'])
        assert result.get(0).commitMessage == 'this is a. test! of commit message'
    }

    @Test
    void createCherryPicksResultEmptyMessageTest() {
        def result = gitDiffParser.createCherryPicksResult(['+ 583c20a21bdb1c9648ed75a6712678c730896882 '])
        assert result.get(0).commitMessage == ''
    }
}
