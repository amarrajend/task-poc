#!/bin/bash

echo "Cleaning up resources..."
kubectl delete -f task-app-deployment.yaml
kubectl delete -f mongodb-deployment.yaml
kubectl delete -f mongodb-pvc.yaml

echo "Checking remaining resources..."
kubectl get pods,svc,pvc

echo "Cleanup complete!"
