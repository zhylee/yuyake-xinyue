package cn.yuyake.xinyue.logic.task;

import cn.yuyake.db.entity.manager.TaskManager;
import cn.yuyake.xinyue.dataconfig.TaskDataConfig;

/**
 * 通关到指定关卡的进度类
 */
public class SpecificBlockTaskProgress implements ITaskProgress {
    @Override
    public void updateProgress(TaskManager taskManager, TaskDataConfig taskDataConfig, Object data) {
        taskManager.setValue((String) data);
    }

    @Override
    public boolean isFinish(TaskManager taskManager, TaskDataConfig taskDataConfig) {
        String value = taskManager.getTaskStringValue();
        if (value == null) {
            return false;
        }
        // 如果当前关卡大于等于目标关卡，说明已通关
        return value.compareTo(taskDataConfig.param) >= 0;
    }

    @Override
    public Object getProgressValue(TaskManager taskManager, TaskDataConfig taskDataConfig) {
        // 如果当前关卡大于等于目标关卡，说明已通关
        return taskManager.getTaskStringValue();
    }
}
