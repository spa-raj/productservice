import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Custom metrics per scenario
const fullTextLatency     = new Trend('full_text_search_duration', true);
const filteredLatency     = new Trend('filtered_sorted_paginated_duration', true);
const multiFieldLatency   = new Trend('multi_field_search_duration', true);
const autocompleteLatency = new Trend('autocomplete_duration', true);
const getByIdLatency      = new Trend('get_by_id_duration', true);
const categoriesLatency   = new Trend('get_categories_duration', true);
const errors              = new Counter('errors');

// ---------------------------------------------------------------------------
// Scenarios — ramp from 1 → 10 → 50 VUs
// ---------------------------------------------------------------------------
export const options = {
  scenarios: {
    full_text_search: {
      executor: 'ramping-vus',
      exec: 'fullTextSearch',
      startVUs: 1,
      stages: [
        { duration: '15s', target: 1 },   // warm-up: 1 VU
        { duration: '30s', target: 10 },   // ramp to 10
        { duration: '30s', target: 10 },   // hold 10
        { duration: '30s', target: 50 },   // ramp to 50
        { duration: '30s', target: 50 },   // hold 50
        { duration: '15s', target: 0 },    // ramp down
      ],
    },
    filtered_sorted_paginated: {
      executor: 'ramping-vus',
      exec: 'filteredSortedPaginated',
      startVUs: 1,
      startTime: '2m30s',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
    multi_field_search: {
      executor: 'ramping-vus',
      exec: 'multiFieldSearch',
      startVUs: 1,
      startTime: '5m',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
    autocomplete: {
      executor: 'ramping-vus',
      exec: 'autocomplete',
      startVUs: 1,
      startTime: '7m30s',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
    get_by_id: {
      executor: 'ramping-vus',
      exec: 'getById',
      startVUs: 1,
      startTime: '10m',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
    get_categories: {
      executor: 'ramping-vus',
      exec: 'getCategories',
      startVUs: 1,
      startTime: '12m30s',
      stages: [
        { duration: '15s', target: 1 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

// ---------------------------------------------------------------------------
// Search terms — rotated per VU iteration to avoid MySQL query cache
// ---------------------------------------------------------------------------
const SEARCH_QUERIES = [
  'leather wallet', 'cotton shirt', 'bamboo bottle', 'silk scarf',
  'titanium watch', 'ceramic mug', 'wooden chair', 'steel belt',
  'glass vase', 'copper lamp', 'velvet pillow', 'canvas bag',
  'marble table', 'carbon cable', 'wool blanket', 'denim jacket',
  'suede boots', 'bronze ring', 'crystal earrings', 'linen towel',
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

// Cache a product ID from the first successful search
let cachedProductId = null;

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

// 1. Full-text search: WHERE name LIKE '%leather wallet%'
export function fullTextSearch() {
  const query = pick(SEARCH_QUERIES);
  const page = Math.floor(Math.random() * 5);
  const res = http.get(
    `${BASE_URL}/search/products?query=${encodeURIComponent(query)}&size=20&page=${page}`
  );
  if (doCheck(res, 'full_text_search', fullTextLatency) && !cachedProductId) {
    try {
      const body = JSON.parse(res.body);
      if (body.products && body.products.length > 0) {
        cachedProductId = body.products[0].id;
      }
    } catch (_) {}
  }
  sleep(0.5);
}

// 2. Filtered + sorted + paginated: category + price range + sort by price
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
  sleep(0.5);
}

// 3. Multi-field search: query + category filter
export function multiFieldSearch() {
  const query = pick(SEARCH_QUERIES).split(' ')[0];
  const category = pick(CATEGORIES);
  const res = http.get(
    `${BASE_URL}/search/products?query=${encodeURIComponent(query)}&categoryName=${encodeURIComponent(category)}&size=20`
  );
  doCheck(res, 'multi_field_search', multiFieldLatency);
  sleep(0.5);
}

// 4. Autocomplete: prefix search
export function autocomplete() {
  const prefix = pick(PREFIXES);
  const res = http.get(
    `${BASE_URL}/search/products/suggest?prefix=${encodeURIComponent(prefix)}&limit=10`
  );
  doCheck(res, 'autocomplete', autocompleteLatency);
  sleep(0.3);
}

// 5. Get product by ID
export function getById() {
  if (!cachedProductId) {
    const searchRes = http.get(`${BASE_URL}/search/products?size=1`);
    try {
      const body = JSON.parse(searchRes.body);
      if (body.products && body.products.length > 0) {
        cachedProductId = body.products[0].id;
      }
    } catch (_) {}
    if (!cachedProductId) {
      sleep(1);
      return;
    }
  }
  const res = http.get(`${BASE_URL}/products/${cachedProductId}`);
  doCheck(res, 'get_by_id', getByIdLatency);
  sleep(0.3);
}

// 6. Get all categories (with product counts)
export function getCategories() {
  const res = http.get(`${BASE_URL}/categories`);
  doCheck(res, 'get_categories', categoriesLatency);
  sleep(0.5);
}
