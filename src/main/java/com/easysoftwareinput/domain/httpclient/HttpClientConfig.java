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

package com.easysoftwareinput.domain.httpclient;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class HttpClientConfig {
     /**
     * 最大连接数.
     */
    private Integer maxTotal = 200;

    /**
     * 路由是对最大连接数的细分.
     * 每个路由基础的连接数.
     */
    private Integer defaultMaxPerRoute = 100;

    /**
     * 连接超时时间.
     */
    private Integer connectTimeout = 5000;

    /**
     * 从连接池中获取连接的超时时间.
     */
    private Integer connectionRequestTimeout = 5000;

    /**
     * 服务器返回数据(response)的时间.
     */
    private Integer socketTimeout = 5000;

    /**
     * 可用空闲连接过期时间.
     * 重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建立
     */
    private Integer validateAfterInactivity = 30000;
}
