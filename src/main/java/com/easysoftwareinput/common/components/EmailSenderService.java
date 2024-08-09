package com.easysoftwareinput.common.components;

import com.easysoftwareinput.common.utils.EmailSenderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderService {
    /**
     * mailsender host.
     */
    @Value("${mail.host}")
    private String host;

    /**
     * mailsender fromAddress.
     */
    @Value("${mail.from}")
    private String from;

    /**
     * mailsender toAddress.
     */
    @Value("${mail.to}")
    private String to;

    /**
     * mailsender senderUsername.
     */
    @Value("${mail.username}")
    private String username;

    /**
     * mailsender senderPassword.
     */
    @Value("${mail.password}")
    private String password;

    /**
     * @param subject   邮件主题
     * @param emailBody 邮件内容
     */
    public void sendMail(String subject, String emailBody) {
        EmailSenderUtil.sendEmail(host, username, password, from, to, subject, emailBody);
    }
}
