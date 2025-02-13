package com.haikal.otel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static CustomMetrics customMetrics;

    public static void main(String[] args) {

        registerBeanMetrics();

        logger.info("Aplicação Java Simples Rodando ");
        while (true) {
            try {
                logger.info("Processando ciclo: {} ", customMetrics.increment());

                logger.info("Processando callEndpointByHttpUrlConncetion - request1");
                callRequestByHttpUrlConncetion();

                logger.info("Processando callRequestByOpenStream - request2");
                callRequestByOpenStream();

                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Encerrando aplicação.");
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

    public interface CustomMetricsMBean {
        int getCounter();
    }

    public static class CustomMetrics implements CustomMetricsMBean {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public int getCounter() {
            return counter.get();
        }

        public int increment() {
            return counter.incrementAndGet();
        }
    }

    private static void callRequestByOpenStream() throws IOException {
        new URL("https://037db4f3-9dd7-4f89-b4cb-789a64679536.mock.pstmn.io/request2").openStream().close();
    }

    private static void callRequestByHttpUrlConncetion() throws IOException {
        URL url = new URL("https://037db4f3-9dd7-4f89-b4cb-789a64679536.mock.pstmn.io/request1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        logger.info("Request status: {}", con.getResponseCode()); //Disparar a requisição
    }
}
