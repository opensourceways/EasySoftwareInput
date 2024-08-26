package com.easysoftwareinput.common.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.Session;

public final class EmailSenderUtil {
    private EmailSenderUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderUtil.class);

    /**
     * @param host      发件人的SMTP服务器地址
     * @param user      发件人的邮箱用户名
     * @param password  发件人的邮箱授权码
     * @param to        收件人电子邮件地址
     * @param from      发件人电子邮件地址
     * @param subject   邮件主题
     * @param emailBody 邮件内容
     */
    public static void sendEmail(final String host, final String user, final String password, String from, String to,
                                 String subject, String emailBody) {
        // SMTP服务器端口号
        final String port = "587";  // 对于TLS
        // final String port = "465";  // 对于SSL


        // 设置邮件服务器的属性
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");  // 启用TLS加密
        // properties.put("mail.smtp.ssl.enable", "true");  // 启用SSL加密（如果需要）

        // 获取默认的Session对象
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // 创建一个默认的MimeMessage对象
            MimeMessage message = new MimeMessage(session);

            // 设置发件人
            message.setFrom(new InternetAddress(from));

            // 设置收件人
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // 设置邮件主题
            message.setSubject(subject);

            // 设置邮件内容
            message.setText(emailBody);

            // 发送邮件
            Transport.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (Exception mex) {
            LOGGER.error("Send message fail! ");
            LOGGER.error(mex.getMessage());
        }

    }


}
