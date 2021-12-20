package cn.yuyake.xinyue.service;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@SpringBootTest(classes = {TestMockBean.class, TestSpyBean.class}) // 在这里指定要测试的类及用到的类
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class) // 必须有这个注解，否则@SpyBean和@MockBean标记的类会为null
@PowerMockIgnore({"org.springframework.*", "javax.*", "org.mockito.*", "org.w3c.*", "org.xml.*"})
@PrepareForTest(TestSpyBean.class)
public class SpringBeanTest extends AbstractTestNGSpringContextTests { // 必须继承AbstractTestNGSpringContextTests这个类
    @SpyBean // 注入要测试的类,使用SpyBean标记
    private TestSpyBean testSpyBean;
    @MockBean // 注入要测试的类，使用MockBean标记
    private TestMockBean testMockBean;

    @BeforeMethod // 10. 重置mock对象
    public void setUp() {
        Mockito.reset(testSpyBean);
        Mockito.reset(testMockBean);
    }

    @ObjectFactory // 在使用Powermock的时候，必须添加这个方法
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

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

    @Test
    public void testMockito() {
        // 1. 使用Mockito，指定返回值
        Mockito.when(testSpyBean.getValue()).thenReturn(30);
        // 2. 使用Mockito，屏蔽某个方法
        // 执行到这个方法，并且参数是a时，不会执行此方法的任何代码
        Mockito.doNothing().when(testMockBean).saveToRedis("a");
        // 4. 使用Mockito验证方法是否执行了
        String str = "abc";
        Mockito.doNothing().when(testMockBean).saveToRedis(str);
        testSpyBean.saveData(str);
        Mockito.verify(testSpyBean).saveData(str); // 默认验证调用了一次
        Mockito.verify(testMockBean, Mockito.times(1)).saveToRedis(str); // 还可以指定验证调用了多少次
        // 5. 使用PowerMock指定静态方法返回值
        PowerMockito.mockStatic(TestSpyBean.class); // mock静态方法
        PowerMockito.when(TestSpyBean.queryValue()).thenReturn(200); // 指定返回值
        int value = TestSpyBean.queryValue();
        assertEquals(value, 200);
        // 8. 使用PowerMock验证静态方法是否执行
        // 验证静态方法是否执行到
        PowerMockito.verifyStatic(TestSpyBean.class);
        // 要验证的静态方法
        TestSpyBean.queryValue();
    }

    // 3. 使用@DataProvider数据驱动测试
    @DataProvider // 提供数据的方法
    public Object[][] data1() {
        return new Object[][]{{1, 100}, {2, 200}, {3, 300}, {4, 500}};
    }

    @Test(dataProvider = "data1") // 指定提供数据的方法名
    public void testDataProvider(int type, int result) {
        int value = testSpyBean.getValue(type);
        assertEquals(value, result);
    }

    @Test // 测试私有方法时，会有异常检查
    public void getName() throws Exception {
        // 6. 使用PowerMockito测试私有方法
        String value = "adssfd";
        // 这里必须重新Spy，否则不会返回指定的值
        testSpyBean = PowerMockito.spy(applicationContext.getBean(TestSpyBean.class));
        PowerMockito.doReturn(value).when(testSpyBean, "getName", Mockito.anyInt());
        // 调用私有方法
        String name = Whitebox.invokeMethod(testSpyBean, "getName", 1);
        assertEquals(name, value);
        // 7. 使用PowerMockito验证私有方法是否执行
        // 验证执行了一次
        PowerMockito.verifyPrivate(testSpyBean).invoke("getName", 1);
        // 在times指定要验证的执行次数
        PowerMockito.verifyPrivate(testSpyBean, Mockito.times(1)).invoke("getName", 1);
    }

    @Test // 使用DoAnswer验证方法的参数
    public void testDoAnswer() throws Exception {
        // 因为测试的是私有方法，这里必须重新spy一次
        testSpyBean = PowerMockito.spy(applicationContext.getBean(TestSpyBean.class));
        PowerMockito.doAnswer(answer -> {
            // 获取方法的参数值
            int value = answer.getArgument(0);
            // 判断是否正确
            assertEquals(value, 12);
            return null;
        }).when(testSpyBean, "getName", Mockito.anyInt());
        testSpyBean.calculate(3);
    }

}
