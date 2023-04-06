package com.openai.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.core.bean.PromptData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/v1/api")
@Api(tags = "openAI接口")
@Slf4j
public class ClientController  {
    @Resource
    RestTemplate restTemplate;

    private static final String OPENAI_URL = "https://api.openai.com/v1/completions";
    //private static final String APP_KEY = "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";
    private static final String APP_KEY = "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";

    @PostMapping("completions")
    @ApiOperation("会话接口")
    ResponseEntity<JSONObject> completions(@RequestBody PromptData promptData) {
        log.info("进入会话.."+promptData.getPrompt());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + APP_KEY);
        HttpEntity<PromptData> httpEntity = new HttpEntity<>(promptData, headers);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(OPENAI_URL, httpEntity, JSONObject.class);
        log.info(jsonObjectResponseEntity.toString());
        return jsonObjectResponseEntity;
    }

    @PostMapping("completions/stream")
    @ApiOperation("实时会话接口")
    void completions(HttpServletResponse response, @RequestBody PromptData promptData) throws IOException {
        promptData.setStream(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + APP_KEY);
        HttpEntity<PromptData> httpEntity = new HttpEntity<>(promptData, headers);
        ResponseEntity<org.springframework.core.io.Resource> resource = restTemplate.postForEntity(OPENAI_URL, httpEntity, org.springframework.core.io.Resource.class);
        org.springframework.core.io.Resource body = resource.getBody();
        if(body == null){
            return ;
        }
       byte[] bytes = new byte[1024];
        InputStream inputStream = body.getInputStream();
        StringBuilder sb = new StringBuilder();
        while(inputStream.read(bytes)>-1){
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        }
        byte[] result = sb.toString().getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(result);
    }
}
