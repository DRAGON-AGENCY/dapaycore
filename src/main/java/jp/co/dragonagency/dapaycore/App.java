package jp.co.dragonagency.dapaycore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DAPayCore アプリケーションのエントリポイント。
 * 組み込み Tomcat での起動（spring-boot:run など）に使用する。
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
