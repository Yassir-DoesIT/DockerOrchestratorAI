package com.example.orchestrator;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PrometheusClientTest {

    @Test
    public void testBasicQuery() throws IOException, ParseException {

        PrometheusClient client = new PrometheusClient();
        double result = client.testQuery();
        System.out.println("Result of the test query that queries \"up\" : " + result);
        Assertions.assertTrue(result > 0, "result should be greater than zero, got: " + result);

    }

    @Test
    public void testServiceCpuUsageQuery() throws IOException, ParseException {

        PrometheusClient client = new PrometheusClient();
        double result = client.getServiceCpuUsage("testStack_test-nginx");
        System.out.println("CPU usage for \"testStack_test-nginx\" : " + result);
        Assertions.assertTrue(result >= 0, "result should be a small fraction or close to 0, got : " + result);

    }

    @Test
    public void testServiceMemoryUsageQuery() throws IOException, ParseException {
        PrometheusClient client = new PrometheusClient();
        double result = client.getServiceMemoryUsage("testStack_test-nginx");
        System.out.println("Memory usage for \"testStack_test-nginx\" : " + result);
        Assertions.assertTrue(result >= 0, "result should be greater than or greater than zero, got: " + result);
    }


}
