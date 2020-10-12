package chrzescijanek.filip.playermarketdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Currency;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
public class SpringFoxConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .directModelSubstitute(Currency.class, String.class)
                .tags(new Tag("Fees", "Fees"))
                .select()
                .paths(regex("/players.*")
                        .or(regex("/teams.*"))
                        .or(regex("/fees.*")))
                .build();
    }

}