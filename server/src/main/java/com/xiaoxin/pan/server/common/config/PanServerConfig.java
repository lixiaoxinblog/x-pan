package com.xiaoxin.pan.server.common.config;

import com.xiaoxin.pan.core.constants.XPanConstants;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.xiaoxin.pan.server")
@Data
public class PanServerConfig {

    @Value("${server.port}")
    private Integer serverPort;
    /**
     * 文件分片的过期天数
     */
    private Integer chunkFileExpirationDays = XPanConstants.ONE_INT;

    /**
     * 分享链接的前缀
     */
    private String sharePrefix = "http://127.0.0.1:" + serverPort + "/share/";
}
