package jp.co.dragonagency.dapaycore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jp.co.dragonagency.dapaycore.interceptor.CsrfProtectionInterceptor;

/**
 * Spring MVC の追加設定。
 * 状態を変更する要求の CSRF トークンを検査するインターセプターを登録する。
 * 静的リソース・favicon・error は検査対象外とする。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String PATTERN_ALL = "/**";
    private static final String PATTERN_STATIC_CSS = "/css/**";
    private static final String PATTERN_STATIC_JS = "/js/**";
    private static final String PATTERN_STATIC_IMAGES = "/images/**";
    private static final String PATTERN_FAVICON = "/favicon.ico";
    private static final String PATTERN_ERROR = "/error";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 参照系メソッドはインターセプター内で検査せず通過させるため、
        // ログイン画面・ルートを対象から除外する必要はない。
        registry.addInterceptor(new CsrfProtectionInterceptor())
                .addPathPatterns(PATTERN_ALL)
                .excludePathPatterns(
                        PATTERN_STATIC_CSS,
                        PATTERN_STATIC_JS,
                        PATTERN_STATIC_IMAGES,
                        PATTERN_FAVICON,
                        PATTERN_ERROR);
    }
}
