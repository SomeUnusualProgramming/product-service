#!/bin/bash

set -e

echo "🚀 Deploying Product Service to Kubernetes..."

KUBE_CONTEXT=${1:-docker-desktop}
echo "Using context: $KUBE_CONTEXT"

kubectl config use-context "$KUBE_CONTEXT" || echo "Could not switch context, using current context"

echo ""
echo "📦 Building Docker image..."
docker build -t product-service:latest .

echo ""
echo "🔄 Applying Kubernetes manifests..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap-secret.yaml
kubectl apply -f k8s/postgres-statefulset.yaml
kubectl apply -f k8s/zookeeper-deployment.yaml
kubectl apply -f k8s/kafka-statefulset.yaml
kubectl apply -f k8s/product-service-deployment.yaml
kubectl apply -f k8s/ingress.yaml

echo ""
echo "⏳ Waiting for deployments to be ready..."
kubectl rollout status deployment/zookeeper -n product-service --timeout=5m
kubectl rollout status statefulset/postgres -n product-service --timeout=5m
kubectl rollout status statefulset/kafka -n product-service --timeout=5m
kubectl rollout status deployment/product-service -n product-service --timeout=5m

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📋 Resources deployed:"
kubectl get all -n product-service

echo ""
echo "🔗 Port forwarding to access the application locally:"
echo "kubectl port-forward -n product-service svc/product-service 8080:8080"
