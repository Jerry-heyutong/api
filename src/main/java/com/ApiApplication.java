package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetSocketAddress;
import java.net.Proxy;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = "com.*")
public class ApiApplication {

	public static void main(String[] args) {
		System.setProperty("proxySet","true");
		System.setProperty("http.proxyHost","thecore.202522.xyz");
		System.setProperty("http.proxyPort","10083");

		SpringApplication.run(ApiApplication.class, args);
	}
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder){
		RestTemplate template = builder.build();

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setProxy(
				new Proxy(
						Proxy.Type.HTTP,
						new InetSocketAddress("127.0.0.1", 7890)  //设置代理服务
				)
		);
		requestFactory.setConnectTimeout(30*1000);
		requestFactory.setReadTimeout(60*1000);
		template.setRequestFactory(requestFactory);
		template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		return template;
	}
}
