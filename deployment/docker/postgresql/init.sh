#!/bin/bash
set -e

# 1. Create Both Databases (Connects to default maintenance DB first)
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE DATABASE polardb_order;
	CREATE DATABASE polardb_catalog;
EOSQL

# 2. Configure CATALOG DB (The "Brain" - Enable Vector Search)
# 🧠 Needs 'vector' for AI Search
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "polardb_catalog" <<-EOSQL
	CREATE EXTENSION IF NOT EXISTS vector;
	CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL

# 3. Configure ORDER DB (The "Ledger" - Standard Relational)
# ⚡ Explicitly targeting "polardb_order"
# 📝 Needs 'uuid-ossp' for IDs, but NO vector math
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "polardb_order" <<-EOSQL
	CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL
