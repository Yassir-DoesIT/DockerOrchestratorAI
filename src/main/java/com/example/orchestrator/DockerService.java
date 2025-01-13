package com.example.orchestrator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.UpdateServiceCmd;
import com.github.dockerjava.api.model.ServiceModeConfig;
import com.github.dockerjava.api.model.ServiceReplicatedModeOptions;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class DockerService {

    private final DockerClient dockerClient;

    public DockerService() {

        dockerClient = DockerClientBuilder.getInstance().build();
    }

    public String getServerVersion() {
        var version = dockerClient.versionCmd().exec();
        return version.getVersion();
    }

    public List<String> listServices() {
        return dockerClient.listServicesCmd()
                .exec()
                .stream()
                .map(service -> service.getSpec().getName())
                .collect(Collectors.toList());
    }

    public long getServiceReplicas(String serviceName) {
        Service service = dockerClient.inspectServiceCmd(serviceName).exec();
        return service.getSpec().getMode().getReplicated().getReplicas();
    }

    public void scaleService(String serviceName, int replicas) {

        Service serviceToScale = dockerClient.inspectServiceCmd(serviceName).exec();
        ServiceSpec updatedSpec = serviceToScale.getSpec();

        updatedSpec.withMode(new ServiceModeConfig()
                .withReplicated(new ServiceReplicatedModeOptions().withReplicas(replicas)));

        UpdateServiceCmd updateCmd = dockerClient.updateServiceCmd(serviceToScale.getId(), updatedSpec).withVersion(serviceToScale.getVersion().getIndex());
        updateCmd.exec();

    }

}
