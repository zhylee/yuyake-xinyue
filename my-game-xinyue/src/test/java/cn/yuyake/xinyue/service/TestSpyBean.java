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

    public int getMockBeanLevel() {
        return testMockBean.getValue();
    }
}
