package cn.yuyake.common.eventsystem;

import org.springframework.context.ApplicationEvent;

public class SpringBootEvent2 extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public SpringBootEvent2(Object source) {
        super(source);
    }
}
