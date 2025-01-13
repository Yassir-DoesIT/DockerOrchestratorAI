package com.example.orchestrator;

import com.example.orchestrator.utility.ResourceLoader;
import org.apache.hc.core5.http.ParseException;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

public class AIAgent {

    private final PrometheusClient prometheusClient;
    private final DockerService dockerService;
    private Consumer<String> logger;
    private boolean aiEnabled = false;
    private Classifier model;
    private Instances data;

    public AIAgent(PrometheusClient prometheusClient, DockerService dockerService, Consumer<String> logger) {
        this.prometheusClient = prometheusClient;
        this.dockerService = dockerService;
        this.logger = logger;
        loadModel();
    }

    private void loadModel() {

        try {
            model = (Classifier) SerializationHelper.read(ResourceLoader.load("replica-model.model"));

            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("cpu_usage"));
            attributes.add(new Attribute("memory_usage_mb"));
            attributes.add(new Attribute("request_rate"));
            attributes.add(new Attribute("latency_ms"));
            attributes.add(new Attribute("current_replicas"));
            attributes.add(new Attribute("optimal_replicas"));

            data = new Instances("replica-model", attributes, 1);
            data.setClassIndex(data.numAttributes() - 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error when loading model : " + e.getMessage());
        }

    }

    public void setAiEnabled(boolean enabled) {
        this.aiEnabled = enabled;
    }

    public void checkAndScale(String serviceName){
        if(!aiEnabled)
            return;
        try {
            double cpuUsage = prometheusClient.getServiceCpuUsage(serviceName) * 100;
            double memoryUsage = prometheusClient.getServiceMemoryUsage(serviceName);
            double requestRate = prometheusClient.getServiceRequestRate(serviceName);
            double latencyMs = prometheusClient.getServiceLatency(serviceName);
            int currentReplicas = (int) dockerService.getServiceReplicas(serviceName);

            Instance instance = new DenseInstance(data.numAttributes());
            instance.setDataset(data);
            instance.setValue(data.attribute("cpu_usage"), cpuUsage);
            instance.setValue(data.attribute("memory_usage_mb"), memoryUsage);
            instance.setValue(data.attribute("request_rate"), requestRate);
            instance.setValue(data.attribute("latency_ms"), latencyMs);
            instance.setValue(data.attribute("current_replicas"), currentReplicas);

            double prediction = model.classifyInstance(instance);
            int predictionToSet = Math.max((int) Math.round(prediction), 1);
            if (predictionToSet != currentReplicas) {
                dockerService.scaleService(serviceName, predictionToSet);
                if (logger != null) {
                    logger.accept("Scaled service : \"" + serviceName + "\" from " + currentReplicas + " to " + predictionToSet);
//                    System.out.println("Scaled service : " + serviceName + " from " + currentReplicas + " to " + predictionToSet);
                }
            }

            System.out.println("Service: " + serviceName +
                    ", CPU: " + cpuUsage +
                    ", Memory: " + memoryUsage +
                    ", Requests: " + requestRate +                //Used System.out.println to debug like a peasant!
                    ", Latency: " + latencyMs +
                   ", Current Replicas: " + currentReplicas +
                    ", Predicted Replicas: " + predictionToSet);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.out.println("Error reading from Prometheus: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}
