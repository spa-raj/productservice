#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$SCRIPT_DIR/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p "$RESULTS_DIR"

# Default: port-forward to EKS productservice
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "============================================"
echo "  MySQL Baseline Benchmark"
echo "  Target: $BASE_URL"
echo "  Dataset: 2M products, 50 categories"
echo "============================================"
echo ""
echo "Scenarios (run sequentially, each ~2.5 min):"
echo "  1. Full-text search   (LIKE '%query%')"
echo "  2. Filtered + sorted + paginated"
echo "  3. Multi-field search (query + category)"
echo "  4. Autocomplete       (prefix LIKE 'Pre%')"
echo "  5. Get by ID          (primary key lookup)"
echo "  6. Get categories     (all 50 categories)"
echo ""
echo "Total estimated time: ~15 minutes"
echo "Results will be saved to: $RESULTS_DIR/"
echo ""

# Run k6
k6 run \
  --env BASE_URL="$BASE_URL" \
  --summary-trend-stats="min,avg,med,p(90),p(95),p(99),max" \
  --out csv="$RESULTS_DIR/mysql_baseline_${TIMESTAMP}.csv" \
  "$SCRIPT_DIR/mysql-baseline.js" \
  2>&1 | tee "$RESULTS_DIR/mysql_baseline_${TIMESTAMP}.log"

echo ""
echo "============================================"
echo "  Benchmark complete!"
echo "  Log:  $RESULTS_DIR/mysql_baseline_${TIMESTAMP}.log"
echo "  CSV:  $RESULTS_DIR/mysql_baseline_${TIMESTAMP}.csv"
echo "============================================"
