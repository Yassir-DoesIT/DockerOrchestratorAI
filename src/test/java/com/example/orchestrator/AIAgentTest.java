package com.example.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class AIAgentTest {

    private PrometheusClient mockitoPrometheus;
    private DockerService mockitoDockerService;
    private Consumer<String> mockitoCallback;
    private AIAgent aiAgent;

    @BeforeEach
    public void setUp() throws Exception {
        mockitoPrometheus = Mockito.mock(PrometheusClient.class);
        mockitoDockerService = Mockito.mock(DockerService.class);
        mockitoCallback = Mockito.mock(Consumer.class);
        when(mockitoDockerService.getServiceReplicas("testStack_test-nginx")).thenReturn(1L);

        try {
            when(mockitoPrometheus.getServiceCpuUsage("testStack_test-nginx")).thenReturn(30.0);
            when(mockitoPrometheus.getServiceMemoryUsage("testStack_test-nginx")).thenReturn(200.0);
            when(mockitoPrometheus.getServiceRequestRate("testStack_test-nginx")).thenReturn(50.0);
            when(mockitoPrometheus.getServiceLatency("testStack_test-nginx")).thenReturn(20.0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        aiAgent = new AIAgent(mockitoPrometheus,mockitoDockerService, mockitoCallback);
        aiAgent.setAiEnabled(true);
    }

    @Test
    public void testCheckandScale() {
        aiAgent.checkAndScale("testStack_test-nginx");
        verify(mockitoDockerService, atMostOnce()).scaleService(eq("testStack_test-nginx"), anyInt());
        verify(mockitoCallback).accept(Mockito.contains("testStack_test-nginx"));
    }
}
