#!/bin/bash
# Script to run e2e tests locally with test database
# This starts the test environment and runs Cypress tests

set -e

echo "Starting E2E test environment..."

# Start services
docker-compose -f docker-compose.e2e.yml up -d

echo "Waiting for services to be ready..."

# Wait for postgres
until docker exec e2e-postgres pg_isready -U test -d testdb > /dev/null 2>&1; do
  echo "Waiting for postgres..."
  sleep 2
done

# Wait for backend
echo "Waiting for backend..."
timeout 120 bash -c 'until curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; do echo "waiting for backend..."; sleep 2; done' || exit 1

# Wait for frontend
echo "Waiting for frontend..."
timeout 60 bash -c 'until curl -f http://localhost:5174 > /dev/null 2>&1; do echo "waiting for frontend..."; sleep 2; done' || exit 1

echo "Services are ready!"

# Setup test data
echo "Setting up test data..."
curl -X POST "http://localhost:8081/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"testuser@example.com","password":"testpassword123","name":"Test User"}' \
  || echo "Test user may already exist"

echo ""
echo "Test environment is ready!"
echo "Backend: http://localhost:8081"
echo "Frontend: http://localhost:5174"
echo ""
echo "Running Cypress tests..."

# Run Cypress tests
cd frontend
CYPRESS_BASE_URL="http://localhost:5174" \
CYPRESS_API_URL="http://localhost:8081" \
npm run test:e2e

TEST_EXIT_CODE=$?

cd ..

# Optionally stop services (comment out if you want to keep them running)
read -p "Stop test services? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo "Stopping services..."
  docker-compose -f docker-compose.e2e.yml down -v
fi

exit $TEST_EXIT_CODE
