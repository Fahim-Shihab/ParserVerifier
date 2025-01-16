package finance.gov.bd.csvParser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CsvParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvParserApplication.class, args);
	}

}
