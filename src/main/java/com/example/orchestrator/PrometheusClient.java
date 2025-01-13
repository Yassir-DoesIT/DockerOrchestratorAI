package com.example.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PrometheusClient {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String urlPrometheus ="http://localhost:9090";
    private Random random = new Random();

    public PrometheusClient() {
        httpClient = HttpClients.createDefault();
        objectMapper = new ObjectMapper();
    }

    private double runQuery(String query) {

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = urlPrometheus + "/api/v1/query?query=" + encodedQuery;
        HttpGet request = new HttpGet(url);

        try(var response = httpClient.execute(request)) {
            int status = response.getCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("Query failed: " + status);
            }
            String responseContent = EntityUtils.toString(response.getEntity());
            JsonNode root = objectMapper.readTree(responseContent);
            JsonNode data = root.path("data").path("result");

            if(data.isArray() && !data.isEmpty()) {
                JsonNode valueNode = data.get(0).findValue("value").get(1);
                return valueNode.asDouble();
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public double testQuery() throws IOException, ParseException {
        return runQuery("up");
    }

    public double getServiceCpuUsage(String serviceName) throws IOException, ParseException {
        String query = String.format(
                "rate(container_cpu_usage_seconds_total{container_label_com_docker_swarm_service_name=\"%s\"}[1m])",serviceName);
        return runQuery(query);
    }

    public double getServiceMemoryUsage(String serviceName) throws IOException, ParseException {
        String query = String.format(
                "container_memory_usage_bytes{container_label_com_docker_swarm_service_name=\"%s\"}", serviceName
        );
        return runQuery(query);
    }

    public double getServiceRequestRate(String serviceName) throws IOException, ParseException {
        return 5 + random.nextInt(11);
    }

    public double getServiceLatency(String serviceName) throws IOException, ParseException {
        return 40.0 + (20.0 * random.nextDouble());
    }

}
