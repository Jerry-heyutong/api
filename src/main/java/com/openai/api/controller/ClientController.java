package com.openai.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.core.bean.ResultEntity;
import com.core.bean.ResultFactory;
import com.openai.api.bean.dto.Chat;
import com.openai.api.bean.dto.Completions;
import com.openai.api.bean.enums.OpenAIAPIEnum;
import com.openai.api.client.SSEClient;
import com.openai.api.handler.AswMsgHandler;
import io.micrometer.core.instrument.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 25267
 */
@RestController
@RequestMapping("/v1/api")
@Api(tags = "openAI接口")
@Slf4j
@EnableAsync
public class ClientController {
    @Resource
    RestTemplate restTemplate;

    public static ConcurrentMap<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    @PostMapping("completions")
    @ApiOperation("补全")
    ResultEntity<String> completions(@RequestBody Completions.PromptData promptData, @RequestParam String apiKey) {
        log.info("进入会话.." + promptData.getPrompt());
        sseCache.putIfAbsent(apiKey, new SseEmitter());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json");
        HttpEntity<Completions.PromptData> httpEntity = new HttpEntity<>(promptData, headers);
        ResponseEntity<JSONObject> jsonObjectResponseEntity = restTemplate.postForEntity(OpenAIAPIEnum.COMPLETIONS.getUrl(), httpEntity, JSONObject.class);
        StringBuilder result = new StringBuilder();
        if (jsonObjectResponseEntity.getBody() != null) {
            Completions.GPTResp resp = JSONObject.parseObject(jsonObjectResponseEntity.getBody().toString(), Completions.GPTResp.class);
            Completions.GPTResp.Choice[] choices = resp.getChoices();
            for (Completions.GPTResp.Choice choice : choices) {
                String text = choice.getText();
                log.info(text);
                result.append(text);
                promptData.setPrompt(promptData.getPrompt() + text);
            }
        } else {
            log.warn(jsonObjectResponseEntity.toString());
        }
        return ResultFactory.success(result.toString());
    }

    @PostMapping("chat/completions")
    @ApiOperation("会话")
    ResultEntity<String> chatCompletions(@RequestBody Chat.Completions req, @RequestParam String apiKey) {
        log.info("USER: " + req.getMessages()[0].getContent());
        req.setStream(false);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json");
        HttpEntity<Chat.Completions> httpEntity = new HttpEntity<>(req, headers);
        ResponseEntity<Chat.ChatResponse> chatResponse = restTemplate.postForEntity(OpenAIAPIEnum.CHAT_COMPLETIONS.getUrl(), httpEntity, Chat.ChatResponse.class);
        if (chatResponse.getBody() != null) {
            String content = chatResponse.getBody().getChoices()[0].getMessage().getContent();
            log.info("AI: " + content);
            return ResultFactory.success(content);
        }
        return ResultFactory.success(chatResponse.toString());
    }

    @PostMapping("chat/completions/stream")
    @ApiOperation("流会话")
    @Async
    ResultEntity chatCompletionsStream(@RequestBody Chat.Completions req, @RequestParam String apiKey, HttpServletResponse resp) throws IOException {
        log.info("=======");
        log.info("USER: " + req.getMessages()[0].getContent());
        req.setStream(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.add("Content-Type", "application/json; charset=UTF-8");
        SSEClient client = new SSEClient();
        Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1",7890));
        sseCache.putIfAbsent(apiKey, new SseEmitter());
        log.info("=======");
        log.info("ChatGPT: ");
        client.sendHttp(OpenAIAPIEnum.CHAT_COMPLETIONS.getUrl(), "POST", headers, JSONObject.toJSONBytes(req),proxy, new AswMsgHandler() {
            @Override
            public void actMsg(InputStream is, String line) {
                if(!StringUtils.isBlank(line)){
                    String data = line.replace("data: ", "");
                    if(data.equals("[DONE]")){
                        return;
                    }
                    Chat.ChatStreamResponse chatResponse = JSONObject.parseObject(data, Chat.ChatStreamResponse.class);
                    String content = chatResponse.getChoices()[0].getDelta().getContent();
                    if(content !=null){
                        log.info(content);
                        try {
                            sseCache.get(apiKey).send(content);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        return ResultFactory.success();
    }
    @GetMapping("chat/completions/stream/sse")
    @ApiOperation("获取实时会话接口返回")
    public SseEmitter handleEvents(@RequestParam("apiKey") String apiKey) {
        SseEmitter sseEmitter = sseCache.get(apiKey);
        if (sseEmitter == null) {
            return null;
        }
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() ->
                sseCache.remove(emitter)
        );
        emitter.onError((Throwable t) -> {
            // Handle the error, e.g. logging or sending an error response
            log.error("Error occurred during server-sent events:", t);
            try {
                // Try to send an error message to the client
                emitter.send("An error occurred!");
            } catch (IOException e) {
                log.error("Failed to send error message:", e);
            }
        });
        sseCache.put(apiKey, emitter);
        emitter.onCompletion(() -> sseCache.remove(apiKey));
        return emitter;
    }
}
