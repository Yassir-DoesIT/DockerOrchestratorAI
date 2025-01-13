package com.example.orchestrator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private TableView<ServiceStatus> servicesTable;
    @FXML private CheckBox aiToggle;
    @FXML private TextArea aiLogArea;


    private DockerService dockerService;
    private PrometheusClient prometheusClient;
    private AIAgent aiAgent;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        dockerService = new DockerService();
        prometheusClient = new PrometheusClient();
        aiAgent = new AIAgent(prometheusClient, dockerService, message -> {
            Platform.runLater(() -> appendAiLog(message));
        });


        TableColumn<ServiceStatus, String> nameCol = new TableColumn<>("Service Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));

        TableColumn<ServiceStatus, Integer> replicasCol = new TableColumn<>("Replicas");
        replicasCol.setCellValueFactory(new PropertyValueFactory<>("replicas"));

        TableColumn<ServiceStatus, Double> cpuCol = new TableColumn<>("CPU Usage");
        cpuCol.setCellValueFactory(new PropertyValueFactory<>("cpuUsage"));

        TableColumn<ServiceStatus, Double> memCol = new TableColumn<>("Memory");
        memCol.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));

        TableColumn<ServiceStatus, Double> reqRateCol = new TableColumn<>("Req/sec");
        reqRateCol.setCellValueFactory(new PropertyValueFactory<>("requestRate"));

        TableColumn<ServiceStatus, Double> latencyCol = new TableColumn<>("Latency (ms)");
        latencyCol.setCellValueFactory(new PropertyValueFactory<>("latency"));

        servicesTable.getColumns().addAll(nameCol, replicasCol, cpuCol, memCol, reqRateCol, latencyCol);

        refreshTable();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), _ -> periodicCheck()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void periodicCheck() {
        for(String name : dockerService.listServices()) {
            aiAgent.checkAndScale(name);
        }
        refreshTable();
    }

    private void appendAiLog(String message) {
        aiLogArea.appendText("[" + LocalTime.now().format(dtf) +"]"+ message + "\n");
    }

    @FXML
    private void onClearLog() {
        aiLogArea.clear();
    }

    @FXML
    private void onScaleUp() {
        ServiceStatus selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int current = (int) dockerService.getServiceReplicas(selected.getServiceName());
            dockerService.scaleService(selected.getServiceName(), current + 1);
            refreshTable();
        }
    }

    @FXML
    private void onScaleDown() {
        ServiceStatus selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int current = (int) dockerService.getServiceReplicas(selected.getServiceName());
            if (current > 1) {
                dockerService.scaleService(selected.getServiceName(), current - 1);
                refreshTable();
            }
        }
    }

    @FXML
    private void onAiToggle() {
        boolean enabled = aiToggle.isSelected();
        aiAgent.setAiEnabled(enabled);
        if (enabled) {
            appendAiLog("AI agent is now ENABLED.");
        } else {
            appendAiLog("AI agent is now DISABLED.");
        }
    }


    @FXML
    private void refreshTable() {
        var list = FXCollections.<ServiceStatus>observableArrayList();
        var serviceNames = dockerService.listServices();

        for (String name : serviceNames) {
            try {
                int replicas = (int) dockerService.getServiceReplicas(name);
                double cpuUsage = prometheusClient.getServiceCpuUsage(name);
                double memoryUsage = prometheusClient.getServiceMemoryUsage(name);
                double reqRate = prometheusClient.getServiceRequestRate(name);
                double latency = prometheusClient.getServiceLatency(name);
                ServiceStatus status = new ServiceStatus(name, replicas, cpuUsage, memoryUsage, reqRate, latency);
                list.add(status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        servicesTable.setItems(list);
    }

}
