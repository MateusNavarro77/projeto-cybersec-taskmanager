#!/usr/bin/env bash
set -euo pipefail

./mvnw -q -DskipTests package -Ddependency-check.skip=true

nohup java -jar target/*.jar --spring.profiles.active=unittest > ./dast-logs/app.log 2>&1 &
APP_PID=$!

cleanup() {
  kill "$APP_PID" 2>/dev/null || true
}
trap cleanup EXIT

echo "Waiting for API docs..."
for i in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:8080/v3/api-docs >/dev/null; then
    break
  fi
  sleep 2
done

docker run --rm --network host \
  -v "$PWD:/zap/wrk/:rw" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-api-scan.py \
  -t http://127.0.0.1:8080/v3/api-docs \
  -f openapi \
  -a \
  -r zap-api-report.html \
  -J zap-api-report.json