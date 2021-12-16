package cn.yuyake.db.entity;

import java.util.concurrent.ConcurrentHashMap;

public class Task {
    // 当前接受的任务id
    private String taskId;
    // 当前的任务进度值
    private Object value;
    // 存储进度的多个值，比如通关y多少x
    private ConcurrentHashMap<String, Object> manyValue = new ConcurrentHashMap<>();

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ConcurrentHashMap<String, Object> getManyValue() {
        return manyValue;
    }

    public void setManyValue(ConcurrentHashMap<String, Object> manyValue) {
        this.manyValue = manyValue;
    }
}
