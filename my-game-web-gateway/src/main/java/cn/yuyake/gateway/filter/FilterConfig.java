package cn.yuyake.gateway.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration // 添加配置注解
@ConfigurationProperties(prefix = "gateway.filter") // 添加配置前缀
public class FilterConfig {

    // 请求权限验证白名单，在这个白名单中的所有配置不需要进行权限验证
    private List<String> whiteRequestUri;

    public List<String> getWhiteRequestUri() {
        return whiteRequestUri;
    }

    public void setWhiteRequestUri(List<String> whiteRequestUri) {
        this.whiteRequestUri = whiteRequestUri;
    }
}
