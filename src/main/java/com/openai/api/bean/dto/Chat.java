package com.openai.api.bean.dto;

import com.openai.api.bean.BaseBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author heyutong
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Chat extends BaseBean {
    @Data
    public static class Completions {
        private String model;
        private Message[] messages;

        /**
         * max_tokens 是一个整数，表示最多可以返回的令牌数
         */
        private Integer max_tokens;

        /**
         * temperature 是一个浮点数，表示随机性的程度。较低的温度会导致较保守的输出，而较高的温度会导致较大胆的输出。
         */
        private Float temperature = 1.0f;

        /**
         * top_p 是一个浮点数，表示从候选输出中选择的最高概率的比例。
         * 顶部概率，表示完成结果的多样性
         */
        private Float top_p = 1.0f;

        /**
         * n 是一个整数，表示要生成的候选输出的数量。
         * 数量，表示要返回的完成结果的数量
         */
        private Integer n;
        /**
         * `stream`：流，表示要返回的完成结果是否应以流的形式返回。
         */
        private Boolean stream = false;

        /**
         * 介于 -2.0 和 2.0 之间的数字。正值会根据新标记到目前为止是否出现在文本中来惩罚它们，从而增加模型讨论新主题的可能性。
         */
        private Float presence_penalty = 1.0f;

        /**
         * 介于 -2.0 和 2.0 之间的数字。正值会根据新标记到目前为止在文本中的现有频率来惩罚新标记，从而降低模型逐字重复同一行的可能性。
         */
        private Float frequency_penalty = 1.0f;

        private String[] stop;
    }

    @Data
    public static class ChatResponse {
        private static final long serialVersionUID = 1L;

        private long created;
        private com.openai.api.bean.dto.Completions.GPTResp.Usage usage;
        private String model;
        private String id;
        private Choice[] choices;
        private String object;

        // getters and setters
        @Data
        public static class Usage {
            private int prompt_tokens;
            private int completion_tokens;
            private int total_tokens;

        }

        @Data
        public static class Choice {
            private Message message;
        }

        private int index;
        private String finish_reason;
    }
    @Data
    public static class ChatStreamResponse {
        private static final long serialVersionUID = 1L;

        private long created;
        private String model;
        private String id;
        private Choice[] choices;
        private String object;

        @Data
        public static class Choice {
            private int index;
            private String finish_reason;
            private Delta delta;
        }

    }

    /**
     * @Auther: HeYuTong
     * @Date: 2023/4/10 16:08
     * @Description:
     */
    @Data
    public static class Message {
        private String role ;
        private String content;
    }
    /**
     * @Auther: HeYuTong
     * @Date: 2023/4/10 16:08
     * @Description:
     */
    @Data
    public static class Delta {
        private String role;
        private String content;
    }
}
