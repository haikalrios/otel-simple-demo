package com.haikal.otel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMetrics implements CustomMetricsMBean {
    private static final Logger logger = LoggerFactory.getLogger(CustomMetrics.class);

    private final AtomicInteger counter = new AtomicInteger(0);

    private static CustomMetrics customMetrics;

    private CustomMetrics(){}

    public static CustomMetrics getInstance(){
        if (customMetrics == null) {
            registerBeanMetrics();
        }
        return customMetrics;
    }

    @Override
    public int getCounter() {
        return counter.get();
    }

    public int increment() {
        return counter.incrementAndGet();
    }
    
    private static void registerBeanMetrics()  {
        try {
            // Criar e registrar um MBean para expor métricas personalizadas via JMX
            customMetrics = new CustomMetrics();
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.example:type=CustomMetrics");
            mbs.registerMBean(customMetrics, name);
        } catch (MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException |
                 MBeanRegistrationException e) {
            logger.error("Bean metric nao pode ser registrado", e);
        }

    }
}
