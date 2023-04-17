package cn.rylan.rest.http;

public enum HTTP {

    GET("GET"),
    POST("POST");

    private final String method;

    HTTP(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
