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
import javax.mail.internet.MimeMessage;


class GmailSender {
    String host;
    String user;
    int port;
    String passwd;
    String status = '';

    public GmailSender() {
        this.host = 'smtp.googlemail.com'
        this.port = 465
        this.user = 'architects@jacky.com'
        this.passwd = 'Fe893cb58f'
    }

    def public simpleMail(to, subject, body) {
        try {
            def props = new Properties();

            props.put("mail.smtp.host", host)
            props.put("mail.smtp.user", user)
            props.put("mail.smtp.port", port)
            props.put("mail.smtp.starttls.enable", "true")
            props.put("mail.smtp.auth", "true")

            def session = Session.getInstance(props, null)
            def msg = new MimeMessage(session)
            msg.setContent(body.toString(), "text/html")
            msg.setSubject(subject)
            msg.setFrom(new InternetAddress(user))
            msg.addRecipients(MimeMessage.RecipientType.TO, (Address[]) to)

            def transport = session.getTransport("smtps")

            status = "Connecting to Gmail"
            transport.connect(host, port, user, passwd)


            transport.sendMessage(msg, msg.getAllRecipients())
            status = 'Message was sent'

            transport.close()
        }
        catch (e) {
            println("An error occurred: " + e.printStackTrace());
        }
    }
}
