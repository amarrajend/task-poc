package com.kaiburr.poc.service;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodBuilder;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerBuilder;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodSpecBuilder;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class KubernetesService {

    private final CoreV1Api api;

    public KubernetesService() throws IOException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        this.api = new CoreV1Api();
    }

    public String createCommandPod(String command, String namespace) {
        try {
            String podName = "command-pod-" + UUID.randomUUID().toString().substring(0, 8);

            V1Container container = new V1ContainerBuilder()
                    .withName("command-runner")
                    .withImage("busybox:latest")
                    .withCommand("sh", "-c", command)
                    .build();

            V1PodSpec podSpec = new V1PodSpecBuilder()
                    .withContainers(container)
                    .withRestartPolicy("Never")
                    .build();

            V1ObjectMeta metadata = new V1ObjectMetaBuilder()
                    .withName(podName)
                    .withNamespace(namespace)
                    .build();

            V1Pod pod = new V1PodBuilder()
                    .withMetadata(metadata)
                    .withSpec(podSpec)
                    .build();

            V1Pod createdPod = api.createNamespacedPod(namespace, pod, null, null, null, null);
            return "Pod created: " + createdPod.getMetadata().getName();

        } catch (ApiException e) {
            throw new RuntimeException("Failed to create Kubernetes pod: " + e.getMessage(), e);
        }
    }
}
