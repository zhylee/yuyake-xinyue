package cn.yuyake.xinyue.logic.task;

import cn.yuyake.db.entity.manager.TaskManager;
import cn.yuyake.xinyue.dataconfig.TaskDataConfig;

public interface ITaskProgress {
    /**
     * 更新任务进度的接口
     *
     * @param taskManager    任务管理类
     * @param taskDataConfig 任务的配置数据
     * @param data           任务进度变化的进度(因为这个值的类型是多个的，有的是int，有的是String，有的是list等，所以使用Object类)
     */
    void updateProgress(TaskManager taskManager, TaskDataConfig taskDataConfig, Object data);

    /**
     * 判断任务的进度是否已完成，表示可以领取任务奖励
     *
     * @param taskManager    任务管理类
     * @param taskDataConfig 任务的配置数据
     * @return 任务的进度是否已完成
     */
    boolean isFinish(TaskManager taskManager, TaskDataConfig taskDataConfig);

    /**
     * 获取任务进行的进度值
     *
     * @param taskManager    任务管理类
     * @param taskDataConfig 任务的配置数据
     * @return 任务进行的进度值
     */
    Object getProgressValue(TaskManager taskManager, TaskDataConfig taskDataConfig);
}
