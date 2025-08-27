package com.example.erpserver.configurations;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.erpserver.security.FiltroToken;

@Configuration
public class FiltroConfig {

    private final FiltroToken filtro;

    public FiltroConfig(FiltroToken filtro) {
        this.filtro = filtro;
    }

    @Bean
    public FilterRegistrationBean<FiltroToken> filterRegistrationBean() {
        FilterRegistrationBean<FiltroToken> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filtro);
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}
