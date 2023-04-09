package com.openai.api;

public enum OpenAIAPIEnum {
    COMPLETIONS("HTTPS://API.OPENAI.COM/V1/COMPLETIONS");

    private String url;

    OpenAIAPIEnum(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
