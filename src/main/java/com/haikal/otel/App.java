package com.haikal.otel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        logger.info("Aplicação Java Simples Rodando ");
        while (true) {
            try {
                logger.info("Processando ciclo: {} ", CustomMetrics.getInstance().increment());

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
