package com.myth.earth.develop.model;

/**
 * 差异结果
 *
 * @author zhouchao
 * @date 2024-01-02 16:58
 */
public class DifferenceResult {

    private String key;

    private String source;

    private String target;

    public DifferenceResult(String key, String source, String target) {
        this.key = key;
        this.source = source;
        this.target = target;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
