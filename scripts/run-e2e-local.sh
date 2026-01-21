#!/bin/bash
# Script to run e2e tests locally with test database
# This starts the test environment and runs Playwright tests

set -e

MODE="headless" # headless | headed | ui
KEEP_SERVICES="false"
INSTALL_BROWSERS="true"
PLAYWRIGHT_ARGS=()

usage() {
  cat <<'EOF'
Usage: ./scripts/run-e2e-local.sh [options] [-- <extra playwright args>]

Options:
  --headed           Run Playwright in a visible browser window
  --ui               Run Playwright UI mode
  --keep             Do not prompt; keep docker-compose services running after tests
  --no-install       Do not run 'npx playwright install' before tests
  -h, --help         Show this help

Examples:
  ./scripts/run-e2e-local.sh
  ./scripts/run-e2e-local.sh --headed
  ./scripts/run-e2e-local.sh --ui
  ./scripts/run-e2e-local.sh --headed -- --project=chromium --grep @smoke
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --headed)
      MODE="headed"
      shift
      ;;
    --ui)
      MODE="ui"
      shift
      ;;
    --keep)
      KEEP_SERVICES="true"
      shift
      ;;
    --no-install)
      INSTALL_BROWSERS="false"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    --)
      shift
      PLAYWRIGHT_ARGS+=("$@")
      break
      ;;
    *)
      echo "Unknown option: $1"
      echo ""
      usage
      exit 2
      ;;
  esac
done

echo "Starting E2E test environment..."

# IMPORTANT: The frontend is built into a static Nginx image; VITE_API_URL must be correct at build-time.
# Locally, the browser runs on your host and must reach the backend via localhost:8081.
export VITE_API_URL="${VITE_API_URL:-http://localhost:8081}"
export VITE_MAPBOX_TOKEN="${VITE_MAPBOX_TOKEN:-test}"

# Start services
docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml up -d --build

echo "Waiting for services to be ready..."

# Wait for postgres (via published port)
until docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml exec -T postgres \
  pg_isready -U test -d testdb > /dev/null 2>&1; do
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
echo "Running Playwright tests..."

# Run Playwright tests
cd frontend
export PLAYWRIGHT_BASE_URL="${PLAYWRIGHT_BASE_URL:-http://localhost:5174}"

echo "Playwright base URL: $PLAYWRIGHT_BASE_URL"
echo "Mode: $MODE"

if [[ "$INSTALL_BROWSERS" == "true" ]]; then
  # Ensure the local Playwright browser binaries exist (required for headed/UI runs).
  npx playwright install chromium
fi

case "$MODE" in
  headless)
    npx playwright test "${PLAYWRIGHT_ARGS[@]}"
    ;;
  headed)
    npx playwright test --headed "${PLAYWRIGHT_ARGS[@]}"
    ;;
  ui)
    npx playwright test --ui "${PLAYWRIGHT_ARGS[@]}"
    ;;
  *)
    echo "Invalid MODE: $MODE"
    exit 2
    ;;
esac

TEST_EXIT_CODE=$?

cd ..

# Optionally stop services (comment out if you want to keep them running)
if [[ "$KEEP_SERVICES" == "true" ]]; then
  echo "Keeping services running (use: docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml down -v)"
else
  read -p "Stop test services? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Stopping services..."
    docker compose -f docker-compose.e2e.yml -f docker-compose.e2e.local.yml down -v
  fi
fi

exit $TEST_EXIT_CODE
