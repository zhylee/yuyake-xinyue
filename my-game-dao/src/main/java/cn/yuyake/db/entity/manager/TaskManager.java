package cn.yuyake.db.entity.manager;

import cn.yuyake.db.entity.Task;

public class TaskManager {
    private final Task task;

    public TaskManager(Task task) {
        this.task = task;
    }

    public boolean isInitTask() {
        return task.getTaskId() != null;
    }

    public void receiveTask(String taskId) {
        task.setTaskId(taskId);
    }

    public void addValue(int value) {
        int newValue = (task.getValue() == null ? 0 : (int) task.getValue()) + value;
        task.setValue(newValue);
    }

    public void setValue(String value) {

        task.setValue(value);
    }

    // 获取Int类型的进度值
    public int getTaskIntValue() {
        return task.getValue() == null ? 0 : (int) task.getValue();
    }

    // 获取String类型的进度值
    public String getTaskStringValue() {
        return task.getValue() == null ? null : (String) task.getValue();
    }

    public void addManyIntValue(String key, int value) {
        Object oldValue = task.getManyValue().get(key);
        int newValue = value;
        if (oldValue != null) {
            newValue += (int) oldValue;
        }
        task.getManyValue().put(key, newValue);
    }

    public int getManyIntValue(String key) {
        Object objValue = task.getManyValue().get(key);
        return objValue == null ? 0 : (int) objValue;
    }

    public String getNowReceiveTaskId() {
        return task.getTaskId();
    }
}
