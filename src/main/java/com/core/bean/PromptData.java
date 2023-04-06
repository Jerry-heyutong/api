package com.core.bean;

public class PromptData {

    private String model;

    /**
     * 字符串，表示用户输入的文本
     */
    private String prompt;

    /**
     *  max_tokens 是一个整数，表示最多可以返回的令牌数
     */
    private Integer max_tokens = 1;

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
    private Integer n = 1;
    /**
     * `stream`：流，表示要返回的完成结果是否应以流的形式返回。
     */
    private Boolean stream = false;
    /**
     * 每个可能的补全的概率值的对数。
     */
    private Object logprobs;
    private String stop;
    public PromptData() {
    }
    public PromptData(String model, String prompt, int max_tokens, int temperature, float top_p, int n, boolean stream, Object logprobs, String stop) {
        this.model = model;
        this.prompt = prompt;
        this.max_tokens = max_tokens;
        this.temperature = temperature;
        this.top_p = top_p;
        this.n = n;
        this.stream = stream;
        this.logprobs = logprobs;
        this.stop = stop;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public float getTop_p() {
        return top_p;
    }

    public void setTop_p(float top_p) {
        this.top_p = top_p;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Object getLogprobs() {
        return logprobs;
    }

    public void setLogprobs(Object logprobs) {
        this.logprobs = logprobs;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }
}
