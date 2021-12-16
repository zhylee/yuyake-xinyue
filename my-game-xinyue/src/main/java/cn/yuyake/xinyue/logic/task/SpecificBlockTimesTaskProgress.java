package cn.yuyake.xinyue.logic.task;

import cn.yuyake.db.entity.manager.TaskManager;
import cn.yuyake.xinyue.dataconfig.TaskDataConfig;

/**
 * 通关指定关卡次数进度管理（指定某个关卡通关多少钱的任务）
 */
public class SpecificBlockTimesTaskProgress implements ITaskProgress {
    @Override
    public void updateProgress(TaskManager taskManager, TaskDataConfig taskDataConfig, Object data) {
        String pointId = (String) data;
        String[] params = taskDataConfig.param.split(",");
        if (pointId.equals(params[0])) {
            // 如果和目标关卡id匹配，测通关次数加1
            taskManager.addManyIntValue(pointId, 1);
        }
    }

    @Override
    public boolean isFinish(TaskManager taskManager, TaskDataConfig taskDataConfig) {
        String[] params = taskDataConfig.param.split(",");
        int value = taskManager.getManyIntValue(params[0]);
        // 如果当前值大于等于目标要求的次数，说明完成任务
        return value >= Integer.parseInt(params[1]);
    }

    @Override
    public Object getProgressValue(TaskManager taskManager, TaskDataConfig taskDataConfig) {
        String[] params = taskDataConfig.param.split(",");
        return taskManager.getManyIntValue(params[0]);
    }
}
