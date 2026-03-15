import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Custom metrics per scenario
const fullTextLatency     = new Trend('full_text_search_duration', true);
const fuzzySearchLatency  = new Trend('fuzzy_search_duration', true);
const descSearchLatency   = new Trend('description_search_duration', true);
const filteredLatency     = new Trend('filtered_sorted_paginated_duration', true);
const multiFieldLatency   = new Trend('multi_field_search_duration', true);
const autocompleteLatency = new Trend('autocomplete_duration', true);
const errors              = new Counter('errors');

// ---------------------------------------------------------------------------
// Scenarios — same intensity as mysql-baseline-light for fair comparison
// Ramp from 1 → 5 → 15 VUs per scenario
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    full_text_search: {
      executor: 'ramping-vus',
      exec: 'fullTextSearch',
      startVUs: 1,
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 15 },
        { duration: '30s', target: 15 },
        { duration: '15s', target: 0 },
      ],
    },
    fuzzy_search: {
      executor: 'ramping-vus',
      exec: 'fuzzySearch',
      startVUs: 1,
      startTime: '2m30s',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 15 },
        { duration: '30s', target: 15 },
        { duration: '15s', target: 0 },
      ],
    },
    description_search: {
      executor: 'ramping-vus',
      exec: 'descriptionSearch',
      startVUs: 1,
      startTime: '5m',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 15 },
        { duration: '30s', target: 15 },
        { duration: '15s', target: 0 },
      ],
    },
    filtered_sorted_paginated: {
      executor: 'ramping-vus',
      exec: 'filteredSortedPaginated',
      startVUs: 1,
      startTime: '7m30s',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 15 },
        { duration: '30s', target: 15 },
        { duration: '15s', target: 0 },
      ],
    },
    multi_field_search: {
      executor: 'ramping-vus',
      exec: 'multiFieldSearch',
      startVUs: 1,
      startTime: '10m',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 15 },
        { duration: '30s', target: 15 },
        { duration: '15s', target: 0 },
      ],
    },
    autocomplete: {
      executor: 'ramping-vus',
      exec: 'autocomplete',
      startVUs: 1,
      startTime: '12m30s',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 5 },
        { duration: '30s', target: 15 },
        { duration: '30s', target: 15 },
        { duration: '15s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
  },
};

// ---------------------------------------------------------------------------
// Search terms — same as MySQL baseline for fair comparison
// ---------------------------------------------------------------------------
const SEARCH_QUERIES = [
  'leather wallet', 'cotton shirt', 'bamboo bottle', 'silk scarf',
  'titanium watch', 'ceramic mug', 'wooden chair', 'steel belt',
  'glass vase', 'copper lamp', 'velvet pillow', 'canvas bag',
  'marble table', 'carbon cable', 'wool blanket', 'denim jacket',
  'suede boots', 'bronze ring', 'crystal earrings', 'linen towel',
];

// Typo variants for fuzzy search (intentional misspellings)
const FUZZY_QUERIES = [
  'lether walet', 'coton shrt', 'bambo bottel', 'silk scraf',
  'titanum wach', 'cermic mug', 'woden chiar', 'stel blet',
  'glas vase', 'coper lmp', 'velve pilow', 'canvs bag',
  'marbl tabel', 'carbn cabl', 'wol blankt', 'dnim jaket',
  'sueed bots', 'bronz rng', 'crystl earings', 'linn towl',
];

// Description-specific terms (not in product name — tests ES description search)
const DESCRIPTION_QUERIES = [
  'high quality', 'everyday use', 'product for', 'genuine',
  'handcrafted', 'premium quality', 'durable', 'lightweight',
];

const PREFIXES = [
  'Pre', 'Cla', 'Mod', 'Vin', 'Ele', 'Rus', 'Sle', 'Bol',
  'Ref', 'Art', 'Lux', 'Com', 'Dur', 'Lig', 'Org', 'Han',
  'Sma', 'Ult', 'Pro', 'Ess', 'Del', 'Eli', 'Roy', 'Nat',
];

const CATEGORIES = [
  "Men's Clothing", "Women's Clothing", "Footwear", "Jewelry",
  "Home Decor", "Furniture", "Kitchen & Dining", "Electronics Accessories",
  "Beauty & Personal Care", "Sports & Outdoors",
];

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function doCheck(res, name, metricTrend) {
  const ok = check(res, {
    [`${name} status 200`]: (r) => r.status === 200,
  });
  metricTrend.add(res.timings.duration);
  if (!ok) errors.add(1);
  return ok;
}

// ---------------------------------------------------------------------------
// Scenario functions
// ---------------------------------------------------------------------------

// 1. Full-text search (same queries as MySQL baseline for comparison)
export function fullTextSearch() {
  const query = pick(SEARCH_QUERIES);
  const page = Math.floor(Math.random() * 5);
  const res = http.get(
    `${BASE_URL}/search/products?query=${encodeURIComponent(query)}&size=20&page=${page}`
  );
  doCheck(res, 'full_text_search', fullTextLatency);
  sleep(1);
}

// 2. Fuzzy search — typo tolerance (ES-only capability, impossible with MySQL LIKE)
export function fuzzySearch() {
  const query = pick(FUZZY_QUERIES);
  const res = http.get(
    `${BASE_URL}/search/products?query=${encodeURIComponent(query)}&size=20&page=0`
  );
  doCheck(res, 'fuzzy_search', fuzzySearchLatency);
  sleep(1);
}

// 3. Description search (MySQL LIKE on CLOB doesn't support lower() — ES advantage)
export function descriptionSearch() {
  const query = pick(DESCRIPTION_QUERIES);
  const res = http.get(
    `${BASE_URL}/search/products?query=${encodeURIComponent(query)}&size=20&page=0`
  );
  doCheck(res, 'description_search', descSearchLatency);
  sleep(1);
}

// 4. Filtered + sorted + paginated (same as MySQL baseline)
export function filteredSortedPaginated() {
  const category = pick(CATEGORIES);
  const minPrice = Math.floor(Math.random() * 500) + 50;
  const maxPrice = minPrice + Math.floor(Math.random() * 5000) + 500;
  const page = Math.floor(Math.random() * 10);
  const sortDir = Math.random() > 0.5 ? 'asc' : 'desc';
  const res = http.get(
    `${BASE_URL}/search/products?categoryName=${encodeURIComponent(category)}&minPrice=${minPrice}&maxPrice=${maxPrice}&sortBy=price&sortDir=${sortDir}&size=20&page=${page}`
  );
  doCheck(res, 'filtered_sorted_paginated', filteredLatency);
  sleep(1);
}

// 5. Multi-field search: query + category filter (same as MySQL baseline)
export function multiFieldSearch() {
  const query = pick(SEARCH_QUERIES).split(' ')[0];
  const category = pick(CATEGORIES);
  const res = http.get(
    `${BASE_URL}/search/products?query=${encodeURIComponent(query)}&categoryName=${encodeURIComponent(category)}&size=20`
  );
  doCheck(res, 'multi_field_search', multiFieldLatency);
  sleep(1);
}

// 6. Autocomplete / typeahead (same as MySQL baseline)
export function autocomplete() {
  const prefix = pick(PREFIXES);
  const res = http.get(
    `${BASE_URL}/search/products/suggest?prefix=${encodeURIComponent(prefix)}&limit=10`
  );
  doCheck(res, 'autocomplete', autocompleteLatency);
  sleep(0.5);
}
