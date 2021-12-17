package cn.yuyake.xinyue.service;

import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@SpringBootTest(classes = {TestMockBean.class, TestSpyBean.class}) // 在这里指定要测试的类及用到的类
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class) // 必须有这个注解，否则@SpyBean和@MockBean标记的类会为null
public class SpringBeanTest extends AbstractTestNGSpringContextTests { // 必须继承AbstractTestNGSpringContextTests这个类
    @SpyBean // 注入要测试的类,使用SpyBean标记
    private TestSpyBean testSpyBean;
    @MockBean // 注入要测试的类，使用MockBean标记
    private TestMockBean testMockBean;

    @Test
    public void testGetValue() {
        // 不指定返回直接，直接调用
        int value = testSpyBean.getValue();
        assertEquals(value, 3);
        int value2 = testMockBean.getValue();
        // 这里会失败，因为没有指定返回值，value2的值是默认值0
        assertEquals(value2, 2);
    }

    @Test
    public void testGetSpecialValue() {
        // 都指定返回值
        Mockito.doReturn(30).when(testSpyBean).getValue();
        Mockito.when(testMockBean.getValue()).thenReturn(100);
        int value = testSpyBean.getValue();
        assertEquals(value, 30);
        int value2 = testMockBean.getValue();
        assertEquals(value2, 100);
    }
}
