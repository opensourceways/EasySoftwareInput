/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/
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
