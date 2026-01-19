#!/bin/bash
# Setup script for E2E test data
# This script creates test users and data in the test database for Cypress e2e tests

set -e

# Database connection details
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5433}
DB_NAME=${DB_NAME:-testdb}
DB_USER=${DB_USER:-test}
DB_PASSWORD=${DB_PASSWORD:-test}

# Backend API URL
API_URL=${API_URL:-http://localhost:8081}

echo "Setting up E2E test data..."
echo "Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo "API URL: $API_URL"

# Wait for backend to be ready
echo "Waiting for backend to be ready..."
until curl -f "$API_URL/actuator/health" > /dev/null 2>&1; do
  echo "Backend not ready, waiting..."
  sleep 2
done

echo "Backend is ready!"

# Create test users via API
echo "Creating test users..."

# Test user 1 (regular user)
curl -X POST "$API_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "testpassword123",
    "name": "Test User"
  }' || echo "User may already exist"

# Test user 2 (admin user - if admin registration is available)
# Note: Admin users typically need to be created directly in the database
# or through a special admin endpoint

echo "E2E test data setup complete!"
echo ""
echo "Test credentials:"
echo "  Email: testuser@example.com"
echo "  Password: testpassword123"
echo "  Username: testuser"
