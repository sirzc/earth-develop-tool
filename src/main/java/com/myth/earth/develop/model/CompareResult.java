package com.myth.earth.develop.model;

import java.util.Collection;

/**
 * 比较结果
 *
 * @author zhouchao
 * @date 2024-01-02 16:55
 */
public class CompareResult {
    /**
     * 只存在于source的key
     */
    private Collection<String> sourceKeys;
    /**
     * 只存在于target的key
     */
    private Collection<String> targetKeys;
    /**
     * key相同内容不同的键
     */
    private Collection<DifferenceResult> differenceResults;

    public Collection<String> getSourceKeys() {
        return sourceKeys;
    }

    public void setSourceKeys(Collection<String> sourceKeys) {
        this.sourceKeys = sourceKeys;
    }

    public Collection<String> getTargetKeys() {
        return targetKeys;
    }

    public void setTargetKeys(Collection<String> targetKeys) {
        this.targetKeys = targetKeys;
    }

    public Collection<DifferenceResult> getDifferenceResults() {
        return differenceResults;
    }

    public void setDifferenceResults(Collection<DifferenceResult> differenceResults) {
        this.differenceResults = differenceResults;
    }

    public boolean hasDiffKeys() {
        if (sourceKeys != null && sourceKeys.size() > 0) {
            return true;
        }

        if (targetKeys != null && targetKeys.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean hasDiffValue() {
        if (differenceResults != null && differenceResults.size() > 0) {
            return true;
        }
        return false;
    }
}
