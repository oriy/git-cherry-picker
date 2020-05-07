package com.jacky.git

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.codehaus.groovy.runtime.StringGroovyMethods

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitDiffParser {

    static final int MAX_ATTEMPTS = 10

    private GitCommandExecutor gitCommandExecutor

    GitDiffParser(GitCommandExecutor gitCommandExecutor) {
        this.gitCommandExecutor = gitCommandExecutor
    }

    public List<CherryPicksResult> findCommitsToCherryPick(String sourceBranch, String targetBranch) {
        GitCommandResult result = gitCommandExecutor.gitCherryDiff(targetBranch, sourceBranch, MAX_ATTEMPTS)

        if (result.isEmptyResponse()) {
            throw new IllegalStateException(String.format("cherry pick diff failed unexpectedly\n%s", result.error))
        }

        String[] rows = result.output.split('\n')
        //regex of  "+ 583c20a21bdb1c9648ed75a6712678c730896882 this is a test of commit message"
        List<CherryPicksResult> cherryPicksResultList = createCherryPicksResult(rows)

        List<CherryPicksResult> checkMessageList = []
        cherryPicksResultList.each {
            if (!it.isExistInBranch()) {
                checkMessageList.add(it)
            }
        }

        GitCommandResult logResult = gitCommandExecutor.gitLog("$sourceBranch..$targetBranch", MAX_ATTEMPTS)
        if (logResult.isEmptyResponse()) {
            println "git log returned empty list"
        }
        GPathResult logXml = convertLogToXml(logResult.output)

        List<CherryPicksResult> commitsToCherryPick = []
        checkMessageList.each {
            if (!getLogOfMessage(it.commitHash, logXml)) {
                commitsToCherryPick.add(it)
            }
        }
        return commitsToCherryPick
    }

    static List<CherryPicksResult> createCherryPicksResult(rows) {
        List<CherryPicksResult> result = []
        //regex of  "+ 583c20a21bdb1c9648ed75a6712678c730896882 this is a test of commit message"
        def myRegularExpression = "(\\+|-) ([a-fA-F0-9]+) (.*)"
        rows.each {
            def matcher = it =~ myRegularExpression
            result.add(new CherryPicksResult(existInBranch: matcher[0][1], commitHash: matcher[0][2], commitMessage: matcher[0][3]))
        }
        return result
    }

    //git log --grep='this is a test of commit message' test...master --pretty=format:"|~entry~||~author~|%an|~!author~||~commit_date~|%cd|~!commit_date~||~message_body~|%s|~!message_body~||~!entry~|"
    static def getLogOfMessage(String commitHash, log) {
        def found = false
        log.entry.each {
            if (it.message_body.text().contains(commitHash) || (it.message_subject.text().contains(commitHash))) {
                found = true
            }
        }
        return found
    }

    def convertLogToXml(String log) {
        String logXml = XmlUtil.escapeXml(log)
        logXml = skipControlCharacters(logXml)
        logXml = createXmlFromLog(logXml)
        logXml = "<root>" + logXml + "</root>"
        if (gitCommandExecutor.isVerbose()) {
            println "$logXml\n"
        }
        def resultXml = new XmlSlurper().parseText(logXml)
        return resultXml
    }

    static String skipControlCharacters(String orig) {
        return StringGroovyMethods.collectReplacements(orig, {
            Character arg -> (arg < 31) ? '' : null
        })
    }

    static String createXmlFromLog(String orig) {
        orig = orig.replace('~commit ', '')
        orig = orig.replace('|~!', '</')
        orig = orig.replace('|~', '<')
        orig = orig.replace('~|', '>')
        orig = orig.replace('</entry>', '</entry>\n')

        return orig
    }
}
