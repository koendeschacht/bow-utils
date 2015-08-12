package be.bagofwords.application;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseApplicationContextFactory implements ApplicationContextFactory {

    private AnnotationConfigApplicationContext applicationContext;
    private List<Object> singletons;
    private MainClass mainClass;

    protected BaseApplicationContextFactory(MainClass mainClass) {
        this.mainClass = mainClass;
        setSaneDefaultsForLog4J();
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.singletons = new ArrayList<>();
        if (mainClass != null) {
            singleton("mainClass", mainClass);
        }
    }

    protected BaseApplicationContextFactory() {
        this(null);
    }

    protected BaseApplicationContextFactory resourceResolver(ResourcePatternResolver resourcePatternResolver) {
        applicationContext.setResourceLoader(resourcePatternResolver);
        return this;
    }

    protected BaseApplicationContextFactory classLoader(ClassLoader classLoader) {
        applicationContext.setClassLoader(classLoader);
        return this;
    }

    protected synchronized BaseApplicationContextFactory singleton(String name, Object object) {
        applicationContext.getBeanFactory().registerSingleton(name, object);
        singletons.add(object);
        return this;
    }

    protected BaseApplicationContextFactory bean(Class _class) {
        applicationContext.register(_class);
        return this;
    }

    protected BaseApplicationContextFactory scan(String prefix) {
        applicationContext.scan(prefix);
        return this;
    }

    @Override
    public void wireApplicationContext() {
        singleton("applicationContextFactory", this);
        applicationContext.refresh();
        applicationContext.registerShutdownHook();
        wireSingletons();
    }

    /**
     * Ugly method, seems we are doing springs job here...
     */

    private void wireSingletons() {
        for (Object singleton : singletons) {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(singleton);
        }
    }

    @Override
    public String getApplicationName() {
        if (mainClass != null) {
            return mainClass.getClass().getSimpleName();
        } else {
            return "";
        }
    }

    @Override
    public AnnotationConfigApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private static void setSaneDefaultsForLog4J() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
    }
}
