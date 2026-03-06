# Plan: Kubernetes Manifests for Product Service

## Context
The productservice needs Kubernetes manifests to deploy alongside the existing userservice in the `vibevault` namespace. The userservice already has k8s manifests at `/home/sparsh-raj/IdeaProjects/userservice/k8s/` which serve as the reference for structure and conventions.

## Files to Create

All files under `k8s/` directory in the productservice project root.

### 1. `k8s/configmap.yaml` — Non-sensitive configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: productservice-config
  namespace: vibevault
  labels:
    app: productservice
    part-of: vibevault
data:
  PORT: "8080"
  DB_URL: "jdbc:mysql://productservice-mysql:3306/productservice?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  ISSUER_URI: "http://userservice.vibevault.svc.cluster.local:80"
  SPRING_PROFILES_ACTIVE: "default"
```

### 2. `k8s/secret.yaml` — Sensitive credentials (base64-encoded)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: productservice-secret
  namespace: vibevault
  labels:
    app: productservice
    part-of: vibevault
type: Opaque
data:
  # productuser
  DB_USERNAME: cHJvZHVjdHVzZXI=
  # productpass
  DB_PASSWORD: cHJvZHVjdHBhc3M=
```

### 3. `k8s/deployment.yaml` — App deployment (2 replicas, RollingUpdate)

Matches userservice pattern exactly:
- **Image:** `productservice:latest`, `imagePullPolicy: IfNotPresent`
- **Port:** containerPort 8080
- **envFrom:** configMapRef `productservice-config` + secretRef `productservice-secret`
- **Resources:** requests cpu=250m/mem=512Mi, limits cpu=500m/mem=768Mi
- **Liveness probe:** HTTP GET `/actuator/health/liveness` port 8080, initialDelay=60s, period=30s, timeout=5s, failureThreshold=3
- **Readiness probe:** HTTP GET `/actuator/health/readiness` port 8080, initialDelay=30s, period=10s, timeout=5s, failureThreshold=3
- **Strategy:** RollingUpdate (maxUnavailable=0, maxSurge=1)
- **Labels:** `app: productservice`, `version: v1`, `part-of: vibevault`

### 4. `k8s/service.yaml` — ClusterIP service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: productservice
  namespace: vibevault
  labels:
    app: productservice
    part-of: vibevault
spec:
  type: ClusterIP
  selector:
    app: productservice
  ports:
    - port: 80
      targetPort: 8080
      protocol: TCP
```

### 5. `k8s/mysql.yaml` — PVC + MySQL Deployment + MySQL Service

Three resources separated by `---`:

**PersistentVolumeClaim:**
- Name: `productservice-mysql-pvc` (differentiated from userservice's `mysql-pvc`)
- 1Gi, ReadWriteOnce
- Labels: `app: productservice-mysql`

**MySQL Deployment:**
- Name: `productservice-mysql`, 1 replica, Recreate strategy
- Image: `mysql:8.0`
- Env: `MYSQL_DATABASE=productservice`, `MYSQL_USER=productuser`, `MYSQL_PASSWORD` from secret (`DB_PASSWORD`), `MYSQL_ROOT_PASSWORD` from secret (`DB_PASSWORD`)
- Resources: same as userservice MySQL (cpu 250m-500m, mem 512Mi-768Mi)
- Volume mount: `/var/lib/mysql` from PVC
- Readiness probe: `mysqladmin ping -h 127.0.0.1 --protocol=TCP` (initialDelay=30s, period=10s, timeout=5s)
- Liveness probe: same command (initialDelay=60s, period=30s, timeout=5s)

**MySQL Service:**
- Name: `productservice-mysql`, ClusterIP, port 3306->3306

## Key Differences from Userservice Reference

| Aspect | Userservice | Productservice |
|--------|-------------|----------------|
| App port | 8081 | 8080 |
| DB host | userservice-mysql | productservice-mysql |
| PVC name | mysql-pvc | productservice-mysql-pvc |
| MySQL user | root only | productuser (non-root) |
| ISSUER_URI | self (`http://userservice:8081`) | points to userservice (`http://userservice.vibevault.svc.cluster.local:80`) |
| No namespace.yaml | N/A | Shared `vibevault` namespace already exists |
| MySQL env vars | MYSQL_ROOT_PASSWORD only | MYSQL_DATABASE + MYSQL_USER + MYSQL_PASSWORD + MYSQL_ROOT_PASSWORD |

## Note on MySQL User Creation
The userservice MySQL only sets `MYSQL_ROOT_PASSWORD` and the app connects as root. For productservice, since credentials are `productuser/productpass`, the MySQL container needs `MYSQL_USER` and `MYSQL_PASSWORD` env vars so MySQL auto-creates that user with access to the `MYSQL_DATABASE`. `MYSQL_ROOT_PASSWORD` is still needed for MySQL to initialize.

## Verification
1. Ensure k8s manifests are valid YAML: `kubectl apply --dry-run=client -f k8s/`
2. Deploy: `kubectl apply -f k8s/` (namespace must exist first from userservice)
3. Check pods: `kubectl get pods -n vibevault`
4. Check MySQL is ready before productservice starts (Flyway will retry on its own)
5. Verify health: `kubectl exec` into a pod and curl `/actuator/health`
