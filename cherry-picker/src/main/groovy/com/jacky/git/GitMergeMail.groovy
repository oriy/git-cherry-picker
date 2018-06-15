package com.jacky.git

import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

/**
 * Created by IntelliJ IDEA.
 * User: alon
 * Date: 4/9/13
 * Time: 3:40 PM
 */
class GitMergeMail {

    def toAddress = []
    StringBuilder body = new StringBuilder()

    public GitMergeMail(AutoCherryPickContext context) {
        context.getEmailList().each { email ->
            addEmailToRecipients(email)
        }
    }

    def addCommitToReport(GString reportMessage, String userEmail) {
        appendBody(reportMessage)
        addEmailToRecipients(userEmail)
    }

    def appendBody(String reportMessage) {
        body.append(reportMessage)
    }

    def appendBody(GString reportMessage) {
        appendBody(reportMessage.toString())
    }

    String getBody() {
        return body.toString()
    }

    def addEmailToRecipients(String userEmail) {
        try {
            InternetAddress authorAddress = new InternetAddress(userEmail)
            authorAddress.validate()
            if (!toAddress.contains(authorAddress)) {
                toAddress.add(authorAddress)
            }
        } catch (AddressException e) {
            println(e.toString())
        }
    }
}
