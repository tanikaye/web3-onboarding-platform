#!/bin/bash

# Exit on error
set -e

# Load environment variables
source .env.testnet

# Create base64 encoded values
DB_URL_B64=$(echo -n "$TESTNET_DB_URL" | base64)
DB_USERNAME_B64=$(echo -n "$TESTNET_DB_USERNAME" | base64)
DB_PASSWORD_B64=$(echo -n "$TESTNET_DB_PASSWORD" | base64)
RPC_URL_B64=$(echo -n "$TESTNET_RPC_URL" | base64)
WS_URL_B64=$(echo -n "$TESTNET_WS_URL" | base64)
JWT_SECRET_B64=$(echo -n "$TESTNET_JWT_SECRET" | base64)

# Create temporary secrets file
cat > secrets.tmp.yaml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: wallet-service-secrets
  namespace: testnet
type: Opaque
data:
  db-url: $DB_URL_B64
  db-username: $DB_USERNAME_B64
  db-password: $DB_PASSWORD_B64
  rpc-url: $RPC_URL_B64
  ws-url: $WS_URL_B64
  jwt-secret: $JWT_SECRET_B64
EOF

# Apply secrets
kubectl apply -f secrets.tmp.yaml

# Clean up
rm secrets.tmp.yaml

echo "Secrets have been created successfully!"