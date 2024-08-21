package xyz.erupt.flow.conf;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.process.listener.*;
import xyz.erupt.flow.service.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ListenerRegister implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (ListenerRegister.applicationContext == null) {
            ListenerRegister.applicationContext = applicationContext;
        }
    }

    /**
     * 注册流程实例的监听器
     */
    @PostConstruct
    public void registerForInstance() {
        ProcessInstanceService service = applicationContext.getBean(ProcessInstanceService.class);
        Class<ExecutableNodeListener>[] classes = new Class[]{
                AfterCreateInstanceListener.class
                , AfterFinishInstanceListener.class
                , AfterStopInstanceListener.class
        };
        for (Class<ExecutableNodeListener> c : classes) {
            this.addListener(service, c);
        }
    }

    /**
     * 注册线程的监听器
     */
    @PostConstruct
    public void registerForExecution() {
        ProcessExecutionService service = applicationContext.getBean(ProcessExecutionService.class);
        Class<ExecutableNodeListener>[] classes = new Class[]{
                AfterCreateExecutionListener.class
                , AfterActiveExecutionListener.class
                , AfterDeactiveExecutionListener.class
                , AfterFinishExecutionListener.class
                , AfterStopExecutionListener.class
        };
        for (Class<ExecutableNodeListener> c : classes) {
            this.addListener(service, c);
        }
    }

    /**
     * 注册活动的监听器
     */
    @PostConstruct
    public void registerForActivity() {
        ProcessActivityService service = applicationContext.getBean(ProcessActivityService.class);
        Class<ExecutableNodeListener>[] classes = new Class[]{
                AfterCreateActivityListener.class
                , AfterActiveActivityListener.class
                , AfterDeactiveActivityListener.class
                , AfterFinishActivityListener.class
                , AfterStopActivityListener.class
        };
        for (Class<ExecutableNodeListener> c : classes) {
            this.addListener(service, c);
        }
    }

    /**
     * 注册任务的监听器
     */
    @PostConstruct
    public void registerForTask() {
        TaskService service = applicationContext.getBean(TaskService.class);
        Class<ExecutableNodeListener>[] classes = new Class[]{
                AfterCreateTaskListener.class
                , AfterActiveTaskListener.class
                , AfterDeactiveTaskListener.class
                , AfterFinishTaskListener.class
                , AfterStopTaskListener.class
                , BeforeAssignTaskListener.class
                , AfterAssignTaskListener.class
                , AfterCompleteTaskListener.class
                , AfterRefuseTaskListener.class
        };
        for (Class<ExecutableNodeListener> c : classes) {
            this.addListener(service, c);
        }
    }

    private void addListener(WithListener service, Class<ExecutableNodeListener> c) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(c);
        List<ExecutableNodeListener> listeners = new ArrayList<>();
        if(beanNamesForType!=null || beanNamesForType.length>0) {
            for (String s : beanNamesForType) {
                ExecutableNodeListener<OaProcessInstance> bean = applicationContext.getBean(s, c);
                listeners.add(bean);
            }
        }
        service.getListenerMap().put(c, listeners);
    }
}
