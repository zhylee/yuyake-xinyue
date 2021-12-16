package cn.yuyake.xinyue.logic.task;

import cn.yuyake.db.entity.manager.TaskManager;
import cn.yuyake.xinyue.logic.functionevent.ConsumeDiamondEvent;
import cn.yuyake.xinyue.logic.functionevent.ConsumeGoldEvent;
import cn.yuyake.xinyue.dataconfig.TaskDataConfig;
import cn.yuyake.xinyue.logic.functionevent.EnterGameEvent;
import cn.yuyake.xinyue.logic.functionevent.PassBlockPointEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    //一般实现的任务进度更新的方法
    public void updateTaskProgress(TaskManager taskManager, int taskType, Object value) {
        if (taskType == 1) {
            // 处理相应的业务
        } else if (taskType == 2) {
            // 处理相应的业务
        } else if (taskType == 3) {
            // 处理相应的业务
        } else if (taskType == 4) {
            // 处理相应的业务
        }
    }

    public boolean isFinishTask(TaskManager taskManager, String taskId) {
        TaskDataConfig taskDataConfig = this.getTaskDataConfig(taskId);
        int taskType = taskDataConfig.taskType;
        if (taskType == 1) {
            // 处理相应的业务
        } else if (taskType == 2) {
            // 处理相应的业务
        } else if (taskType == 3) {
            // 处理相应的业务
        } else if (taskType == 4) {
            // 处理相应的业务
        }
        return false;
    }

    @EventListener
    public void EnterGameEvent(EnterGameEvent event) {
        // 进入游戏的时候，判断一下任务有没有实始化，没有初始化的，自动接收第一个任务
        TaskManager taskManager = event.getPlayerManager().getTaskManager();
        if (!taskManager.isInitTask()) {
            // 获取第一个任务的任务id
            String taskId = "1001";
            taskManager.receiveTask(taskId);
        }
    }

    @EventListener // 接收金币消耗事件
    public void consumeGold(ConsumeGoldEvent event) {
        this.updateTaskProgress(event.getPlayerManager().getTaskManager(), EnumTaskType.ConsumeGold, event.getGold());
    }

    @EventListener
    public void consumeDiamond(ConsumeDiamondEvent event) {
        this.updateTaskProgress(event.getPlayerManager().getTaskManager(), EnumTaskType.ConsumeDiamond, event.getDiamond());
    }

    @EventListener // 通关事件影响多个任务类型的进度
    public void passBlockPoint(PassBlockPointEvent event) {
        this.updateTaskProgress(event.getPlayerManager().getTaskManager(),EnumTaskType.PassBlockPoint, event.getPointId());
        this.updateTaskProgress(event.getPlayerManager().getTaskManager(),EnumTaskType.PassBlockPointTimes, event.getPointId());

    }

    // 统一更新任务进度的方法
    private void updateTaskProgress(TaskManager taskManager, EnumTaskType taskType, Object value) {
        String taskId = taskManager.getNowReceiveTaskId();
        TaskDataConfig taskDataConfig = this.getTaskDataConfig(taskId);
        if (taskDataConfig.taskType == taskType.getType()) {
            // 如果事件更新的任务类型，与当前接受的任务类型一致，更新任务进度
            taskType.getTaskProgress().updateProgress(taskManager, taskDataConfig, value);
        }
    }


    // 根据taskId获取这个taskId对应的配置数据，这里直模拟返回一个
    private TaskDataConfig getTaskDataConfig(String taskId) {
        return new TaskDataConfig();
    }
}
