#!/bin/bash

echo "Waiting for Ollama to be healthy..."
for i in {1..60}; do
    if curl -f http://localhost:11434/api/tags > /dev/null 2>&1; then
        echo "Ollama is healthy!"
        echo "Pulling mistral model..."
        curl -X POST http://localhost:11434/api/pull -d '{"name":"mistral"}' -H "Content-Type: application/json" 2>/dev/null || true
        echo "Model pull initiated in background"
        exit 0
    fi
    echo "Waiting for Ollama... attempt $i/60"
    sleep 1
done

echo "Ollama did not become healthy in time"
exit 1
