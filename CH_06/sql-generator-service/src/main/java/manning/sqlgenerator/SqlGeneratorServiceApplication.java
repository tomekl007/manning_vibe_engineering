package manning.sqlgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SqlGeneratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlGeneratorServiceApplication.class, args);
    }
}
