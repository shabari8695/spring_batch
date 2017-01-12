package hello;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackageClasses = ApplicationConfiguration.class)
public class ApplicationConfiguration {
}
