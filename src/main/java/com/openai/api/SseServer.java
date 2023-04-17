package com.openai.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: HeYuTong
 * @Date: 2023/4/10 18:21
 * @Description:
 */
@Slf4j
public class SseServer {

        private static AtomicInteger count = new AtomicInteger(0);

        private static Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

        public static SseEmitter connect(String userId) {
            SseEmitter sseEmitter = new SseEmitter(0L);
            sseEmitter.onCompletion(() -> {
                log.info("结束连接：{}", userId);
                removeUser(userId);
            });
            sseEmitter.onError(throwable -> {
                log.info("连接异常：{}", userId);
                removeUser(userId);
            });
            sseEmitter.onTimeout(() -> {
                log.info("连接超时：{}", userId);
                removeUser(userId);
            });
            sseEmitterMap.put(userId, sseEmitter);
            count.getAndIncrement();
            log.info("创建新的sse连接，当前用户：{}", userId);
            return sseEmitter;
        }


        public static void sendMessage(String userId, Object message) {
            if (sseEmitterMap.containsKey(userId)) {
                try {
                    sseEmitterMap.get(userId).send(message);
                    log.info("SSE 发送信息成功！id = {} , message: {} ", userId, message);
                } catch (IOException e) {
                    log.error("[{}]推送异常:{}", userId, e.getMessage());
                    removeUser(userId);
                }
            } else {
                log.warn("SSE 发送信息异常，用户不存在：id = {} ", userId);
            }
        }

        private static void removeUser(String userId) {
            sseEmitterMap.remove(userId);
            count.getAndDecrement();
        }

}
