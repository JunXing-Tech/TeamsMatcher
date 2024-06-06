package tech.jxing.teams_matcher.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author JunXing
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名
                // TODO .allowedOrigins("http://1.12.221.240:5173")
                .allowedOrigins("http://localhost:5173")
                // 是否允许证书 不再默认开启
                .allowCredentials(true)
                // 设置允许的请求方式
                .allowedMethods("*")
                // 跨域允许时间
                .maxAge(3600);
    }
}
