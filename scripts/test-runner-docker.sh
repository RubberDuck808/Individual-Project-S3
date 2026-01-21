#!/bin/bash
# Test script to verify GitLab runner Docker socket access
# This simulates what the CI pipeline does

set -e

echo "=========================================="
echo "GitLab Runner Docker Socket Test"
echo "=========================================="
echo ""

# Test 1: Check if Docker socket exists
echo "1. Checking Docker socket..."
if [ -S /var/run/docker.sock ]; then
    echo "   ✓ Docker socket found at /var/run/docker.sock"
    ls -la /var/run/docker.sock
else
    echo "   ✗ Docker socket NOT found at /var/run/docker.sock"
    exit 1
fi
echo ""

# Test 2: Check Docker daemon access
echo "2. Testing Docker daemon connection..."
if docker info > /dev/null 2>&1; then
    echo "   ✓ Docker daemon is accessible"
    docker info | head -5
else
    echo "   ✗ Cannot connect to Docker daemon"
    echo "   Error details:"
    docker info 2>&1 || true
    exit 1
fi
echo ""

# Test 3: Check Docker Compose
echo "3. Testing Docker Compose..."
if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
    echo "   ✓ Docker Compose available"
    docker compose version || docker-compose --version
else
    echo "   ✗ Docker Compose not available"
    echo "   Installing docker-cli-compose..."
    apk add --no-cache docker-cli-compose 2>&1 || echo "   (Installation would happen in CI)"
fi
echo ""

# Test 4: Test basic Docker operations
echo "4. Testing basic Docker operations..."
echo "   Running test container..."
docker run --rm docker:24 echo "   ✓ Container execution works"
echo ""

# Test 5: Test docker-compose (if available)
echo "5. Testing docker-compose with e2e config..."
if docker compose version &> /dev/null || command -v docker-compose &> /dev/null; then
    cd "$(dirname "$0")/.."
    if [ -f "docker-compose.e2e.yml" ]; then
        echo "   Validating docker-compose.e2e.yml..."
        docker compose -f docker-compose.e2e.yml config > /dev/null 2>&1 && \
            echo "   ✓ docker-compose.e2e.yml is valid" || \
            echo "   ✗ docker-compose.e2e.yml has errors"
    else
        echo "   ⚠ docker-compose.e2e.yml not found (expected if not in project root)"
    fi
else
    echo "   ⚠ Docker Compose not available, skipping"
fi
echo ""

# Test 6: Check environment variables (CI simulation)
echo "6. Simulating CI environment..."
export DOCKER_HOST="unix:///var/run/docker.sock"
export DOCKER_TLS_CERTDIR=""
echo "   DOCKER_HOST=$DOCKER_HOST"
echo "   DOCKER_TLS_CERTDIR=$DOCKER_TLS_CERTDIR"
if docker info > /dev/null 2>&1; then
    echo "   ✓ Docker works with CI environment variables"
else
    echo "   ✗ Docker fails with CI environment variables"
fi
echo ""

# Test 7: Test network creation (simulating docker-compose network)
echo "7. Testing Docker network operations..."
NETWORK_NAME="test-runner-network-$(date +%s)"
if docker network create "$NETWORK_NAME" > /dev/null 2>&1; then
    echo "   ✓ Can create Docker networks"
    docker network rm "$NETWORK_NAME" > /dev/null 2>&1
    echo "   ✓ Can remove Docker networks"
else
    echo "   ✗ Cannot create Docker networks"
fi
echo ""

# Test 8: Check user permissions
echo "8. Checking user and permissions..."
echo "   Current user: $(whoami)"
echo "   User ID: $(id)"
echo "   Docker socket owner: $(stat -c '%U:%G' /var/run/docker.sock 2>/dev/null || echo 'unknown')"
echo ""

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo "If all tests passed, your runner should work with the CI pipeline."
echo "If any test failed, check the error messages above."
echo ""
