# Or deploy manually
kubectl apply -f mongodb-pvc.yaml
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f rbac.yaml
kubectl apply -f task-app-deployment.yaml

# Check everything is running
kubectl get all
