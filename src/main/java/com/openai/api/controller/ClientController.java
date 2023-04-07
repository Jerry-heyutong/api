package com.openai.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.core.bean.chatgpt.GPTResp;
import com.core.bean.chatgpt.PromptData;
import com.core.bean.ResultEntity;
import com.core.bean.ResultFactory;
import com.openai.api.component.ChatSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/v1/api")
@Api(tags = "openAI接口")
@Slf4j
public class ClientController {
    public ConcurrentHashMap<String, ChatSession> sessionMap = new ConcurrentHashMap<>(16);
    @Resource
    RestTemplate restTemplate;

    private static final String OPENAI_URL = "https://api.openai.com/v1/completions";
    private String apiKey = "";

    @PostMapping("completions")
    @ApiOperation("会话接口")
    ResultEntity<String> completions(@RequestBody PromptData promptData, @RequestParam String apiKey) {
        log.info("进入会话.." + promptData.getPrompt());
        if (apiKey != null) {
            this.apiKey = apiKey;
        }
        ChatSession chatSession;
        //首先获取上下文
        if (sessionMap.contains(apiKey)) {
            chatSession = sessionMap.get(apiKey);
            StringBuilder prompts = new StringBuilder(chatSession.getPromptData().getPrompt());
            String inputPromt = promptData.getPrompt();
            prompts.append(inputPromt);
            promptData.setPrompt(prompts.toString());
        }else{
            chatSession = new ChatSession();
            chatSession.setPromptData(promptData);
            chatSession.setApiKey(apiKey);
            sessionMap.put(chatSession.getApiKey(),chatSession);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json");
        HttpEntity<PromptData> httpEntity = new HttpEntity<>(promptData, headers);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(OPENAI_URL, httpEntity, JSONObject.class);
        StringBuilder result = new StringBuilder();
        if (jsonObjectResponseEntity.getBody() != null) {
            GPTResp resp = JSONObject.parseObject(jsonObjectResponseEntity.getBody().toString(), GPTResp.class);
            GPTResp.Choice[] choices = resp.getChoices();
            for (GPTResp.Choice choice : choices) {
                String text = choice.getText();
                log.info(text);
                result.append(text);
                promptData.setPrompt(promptData.getPrompt()+text);
            }
        } else {
            log.warn(jsonObjectResponseEntity.toString());
        }
        return ResultFactory.success(result.toString());
    }

    @PostMapping("completions/stream")
    @ApiOperation("实时会话接口")
    void completions(HttpServletResponse response, @RequestBody PromptData promptData) throws IOException {
        promptData.setStream(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        HttpEntity<PromptData> httpEntity = new HttpEntity<>(promptData, headers);
        ResponseEntity<org.springframework.core.io.Resource> resource = restTemplate.postForEntity(OPENAI_URL, httpEntity, org.springframework.core.io.Resource.class);
        org.springframework.core.io.Resource body = resource.getBody();
        if (body == null) {
            return;
        }
        byte[] bytes = new byte[1024];
        InputStream inputStream = body.getInputStream();
        StringBuilder sb = new StringBuilder();
        while (inputStream.read(bytes) > -1) {
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        }
        byte[] result = sb.toString().getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(result);
    }
}
