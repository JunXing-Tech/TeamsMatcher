package tech.jxing.teams_matcher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author JunXing
 */
@SpringBootApplication
@MapperScan("tech.jxing.teams_matcher.mapper")
@EnableScheduling
public class TeamsMatcherBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeamsMatcherBackendApplication.class, args);
    }
}
