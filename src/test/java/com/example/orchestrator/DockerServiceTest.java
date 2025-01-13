package com.example.orchestrator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DockerServiceTest {

    @Test
    public void testDockerService() {
        DockerService dockerService = new DockerService();
        String version = dockerService.getServerVersion();
        System.out.println(version);
        Assertions.assertNotNull(version, "Docker version shouldn't be null");
        Assertions.assertFalse(version.isEmpty(), "Docker version should not be empty");
    }

    @Test
    public void testServiceList() {

        DockerService dockerService = new DockerService();
        List<String> services = dockerService.listServices();
        System.out.println("Services are :" + services);
        Assertions.assertTrue(services.contains("testStack_test-nginx"));
    }

    @Test
    public void testScaleService() throws InterruptedException {
        DockerService dockerService = new DockerService();
        String serviceName = "testStack_test-nginx";
        int initReplicas = (int) dockerService.getServiceReplicas(serviceName);
        System.out.println("Replicas of \"" + serviceName + "\" before scaling : " + initReplicas);
        dockerService.scaleService(serviceName,initReplicas + 1);
        Thread.sleep(5000);

        int newReplicas = (int) dockerService.getServiceReplicas(serviceName);
        System.out.println("Replicas of \"" + serviceName + "\" after scaling up : " + newReplicas);
        Assertions.assertEquals(initReplicas + 1, newReplicas, "Replicas should've increased by 1");

        dockerService.scaleService(serviceName,initReplicas);
        Thread.sleep(5000);
        int finalReplicas = (int) dockerService.getServiceReplicas(serviceName);
        System.out.println("Final replicas of \"" + serviceName + "\" after scaling down again : " + finalReplicas);
        Assertions.assertEquals(initReplicas, finalReplicas, "Replicas should be back to the original count");
    }

}
