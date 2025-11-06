#!/bin/bash

# Script completo para executar todos os testes de carga Gatling
# Este script automatiza o processo de setup, execução e reset dos dados

set -e

echo "=========================================="
echo "Gatling Load Tests - Complete Suite"
echo "=========================================="
echo ""

# Verifica se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "ERROR: Docker is not running."
    echo "Please start Docker and try again."
    exit 1
fi

# Verifica se docker-compose.yml existe
if [ ! -f "docker-compose.yml" ]; then
    echo "ERROR: docker-compose.yml not found."
    echo "Please run this script from the project root directory."
    exit 1
fi

# 1. Setup inicial
echo "=== Step 1: Starting services ==="
docker-compose up -d

echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Verificar se o container está saudável
if ! docker exec postgres-ticket pg_isready -U ticketuser -d ticketdb > /dev/null 2>&1; then
    echo "WARNING: PostgreSQL may not be ready yet. Waiting a bit more..."
    sleep 5
fi

echo "✓ Services are running"
echo ""

# 2. Criar dados de teste
echo "=== Step 2: Seeding test data ==="
./seed-test-data.sh
echo ""

# 3. Teste 1: Booking Flow
echo "=== Step 3: Running Booking Flow Test ==="
echo "Testing realistic user behavior with 50 concurrent users..."
./run-gatling-booking.sh -u 50 -d 60
echo ""
echo "✓ Booking Flow Test completed"
echo ""

# Reset dados
echo "Resetting test data..."
./reset-test-data.sh
echo ""

# 4. Teste 2: Race Condition
echo "=== Step 4: Running Race Condition Test ==="
echo "Testing concurrency with 100 users competing for same seats..."
./run-gatling-race.sh -u 100
echo ""
echo "✓ Race Condition Test completed"
echo ""

# Reset dados
echo "Resetting test data..."
./reset-test-data.sh
echo ""

# 5. Teste 3: Stress Test (opcional - comentar se não quiser executar)
echo "=== Step 5: Running Stress Test ==="
echo "WARNING: This test will put significant load on your system!"
read -p "Continue with stress test? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./run-gatling-stress.sh -n 10 -s 50 -p 200
    echo ""
    echo "✓ Stress Test completed"
    echo ""

    # Reset dados
    echo "Resetting test data..."
    ./reset-test-data.sh
    echo ""
else
    echo "Skipping stress test."
    echo ""
fi

# Resumo final
echo "=========================================="
echo "All Tests Completed!"
echo "=========================================="
echo ""
echo "Test reports are available in: target/gatling/"
echo ""
echo "To view the latest report:"
echo "  open target/gatling/\$(ls -t target/gatling/ | head -1)/index.html"
echo ""
echo "To view application logs:"
echo "  docker-compose logs -f"
echo ""
echo "To stop services:"
echo "  docker-compose down"
echo "=========================================="
