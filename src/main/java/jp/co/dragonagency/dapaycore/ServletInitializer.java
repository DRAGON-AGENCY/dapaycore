package jp.co.dragonagency.dapaycore;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 外部 Tomcat へ WAR としてデプロイする際の初期化クラス。
 * デプロイ時にアプリケーション設定クラス（App）を登録する。
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(App.class);
    }
}
