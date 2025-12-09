#!/bin/bash

set -e

echo "🗑️  Removing Product Service from Kubernetes..."

KUBE_CONTEXT=${1:-docker-desktop}
echo "Using context: $KUBE_CONTEXT"

kubectl config use-context "$KUBE_CONTEXT" || echo "Could not switch context, using current context"

echo ""
echo "⚠️  This will delete all resources in the product-service namespace"
read -p "Are you sure? (yes/no) " -n 3 -r
echo ""

if [[ $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Deleting resources..."
    kubectl delete namespace product-service --ignore-not-found=true
    echo "✅ Deleted successfully"
else
    echo "❌ Cancelled"
fi
