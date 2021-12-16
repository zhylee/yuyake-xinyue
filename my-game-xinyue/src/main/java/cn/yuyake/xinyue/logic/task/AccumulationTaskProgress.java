package cn.yuyake.xinyue.logic.task;

import cn.yuyake.db.entity.manager.TaskManager;
import cn.yuyake.xinyue.dataconfig.TaskDataConfig;

/**
 * 数值累计型进度值管理
 */
public class AccumulationTaskProgress implements ITaskProgress {
    @Override // 更新任务进度
    public void updateProgress(TaskManager taskManager, TaskDataConfig taskDataConfig, Object data) {
        taskManager.addValue((int)data);
    }

    @Override // 判断任务是否完成
    public boolean isFinish(TaskManager taskManager, TaskDataConfig taskDataConfig) {
        int target = Integer.parseInt(taskDataConfig.param);
        int value = taskManager.getTaskIntValue();
        return value >= target;
    }

    @Override // 获取任务进度
    public Object getProgressValue(TaskManager taskManager, TaskDataConfig taskDataConfig) {
        return taskManager.getTaskIntValue();
    }
}
