#!/bin/bash

set -e

echo "Waiting for Ollama to start..."
for i in {1..60}; do
    if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
        echo "Ollama is ready!"
        break
    fi
    echo "Attempt $i/60: Waiting for Ollama..."
    sleep 1
done

echo "Pulling Mistral model..."
ollama pull mistral

echo "Model ready!"
