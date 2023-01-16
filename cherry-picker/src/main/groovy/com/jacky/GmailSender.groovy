package com.jacky

import javax.mail.Address
import javax.mail.Session

/**
 * Created by IntelliJ IDEA.
 * User: alon
 * Date: 7/16/12
 * Time: 3:17 PM
 */
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

import static com.jacky.git.ConfigurationUtil.configuration
import static com.jacky.git.ConfigurationUtil.decryptPass;


class GmailSender {
    String host
    String email
    int port
    String password
    String status = ''

    public GmailSender() {
        this.host = 'smtp.googlemail.com'
        this.port = 465
        this.email = configuration.gmailUser
        this.password = decryptPass(configuration.gmailPass)
    }

    def public simpleMail(to, subject, body) {
        try {
            def props = new Properties();

            props.put("mail.smtp.host", host)
            props.put("mail.smtp.user", email)
            props.put("mail.smtp.port", port)
            props.put("mail.smtp.starttls.enable", true)
            props.put("mail.smtp.auth", true)

            def session = Session.getInstance(props, null)
            def msg = new MimeMessage(session)
            msg.setContent(body.toString(), "text/html")
            msg.setSubject(subject)
            msg.setFrom(new InternetAddress(email))
            msg.addRecipients(MimeMessage.RecipientType.TO, (Address[]) to)

            def transport = session.getTransport("smtps")

            status = "Connecting to Gmail"
            transport.connect(host, port, email, password)

            transport.sendMessage(msg, msg.getAllRecipients())
            status = 'Message was sent'

            transport.close()
        }
        catch (e) {
            println("An error occurred: " + e.printStackTrace());
        }
    }
}
