package com.example.orchestrator;

public class ServiceStatus {

    private String serviceName;
    private int replicas;
    private double cpuUsage;
    double memoryUsage;
    double requestRate;
    double latency;

    public ServiceStatus(String serviceName, int replicas, double cpuUsage, double memoryUsage,
                         double requestRate, double latency) {
        this.serviceName = serviceName;
        this.replicas = replicas;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.requestRate = requestRate;
        this.latency = latency;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getReplicas() {
        return replicas;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public double getRequestRate() {
        return requestRate;
    }

    public double getLatency() {
        return latency;
    }
}
