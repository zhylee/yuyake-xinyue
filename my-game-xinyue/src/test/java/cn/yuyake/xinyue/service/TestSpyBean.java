package cn.yuyake.xinyue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestSpyBean {
    @Autowired
    private TestMockBean testMockBean;

    public int getValue() {
        return 3;
    }

    private String getName(int a) {
        return "a" + a;
    }

    public int getMockBeanLevel() {
        return testMockBean.getValue();
    }

    public int getValue(int type) {
        switch (type) {
            case 1:
                return 100;
            case 2:
                return 200;
            case 3:
                return 300;
            default:
                return 500;
        }
    }

    public void saveData(String value) {
        if (value != null && !value.isEmpty()) {
            testMockBean.saveToRedis(value);
        }
    }

    public static int queryValue() {
        return 3;
    }

    // 计算出结果，并将结果传给其他的方法
    public void calculate(int a) {
        int value = (a + 3) * 2;
        this.getName(value);
    }
}
