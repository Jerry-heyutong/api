package com.openai.api.bean.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author heyutong
 */
@Data
public class Completions {
    @Data
    public static class PromptData {

        private String model;

        /**
         * 字符串，表示用户输入的文本
         */
        private String prompt;

        /**
         *  max_tokens 是一个整数，表示最多可以返回的令牌数
         */
        private Integer max_tokens ;

        /**
         *  temperature 是一个浮点数，表示随机性的程度。较低的温度会导致较保守的输出，而较高的温度会导致较大胆的输出。
         */
        private Integer temperature;

        /**
         * top_p 是一个浮点数，表示从候选输出中选择的最高概率的比例。
         * 顶部概率，表示完成结果的多样性
         */
        private float top_p;

        /**
         * n 是一个整数，表示要生成的候选输出的数量。
         * 数量，表示要返回的完成结果的数量
         */
        private Integer n;
        /**
         * `stream`：流，表示要返回的完成结果是否应以流的形式返回。
         */
        private Boolean stream ;
        /**
         * 每个可能的补全的概率值的对数。
         */
        private Object logprobs;
        private String[] stop;
    }

    @Data
    public static class GPTResp {
        private static final long serialVersionUID = 1L;

        private long created;
        private Usage usage;
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

            //getters and setters
        }

        @Data
        public static class Choice {
            private String text;
        }

        private int index;
        private String finish_reason;

        //getters and setters
    }
}
