-- 1. Create the Database for Order Service (Standard Relational)
CREATE DATABASE polardb_order;

-- 2. Create the Database for Catalog Service (Vector Enabled)
CREATE DATABASE polardb_catalog;

-- 3. SETUP ORDER DB
-- We switch context to 'polardb_order' to ensure tables/users are created there
\c polardb_order;
-- Enable UUID support (Standard for ID generation)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 4. SETUP CATALOG DB (The Critical Part)
-- We switch context to 'polardb_catalog'
\c polardb_catalog;

-- ⚡ ENABLE INTELLIGENCE
-- This command fails on standard Postgres, but works on pgvector image
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 5. GRANT PERMISSIONS (If using a specific non-root user)
-- Since your docker-compose sets POSTGRES_USER=user,
-- 'user' already owns these DBs, so explicit grants might not be strictly necessary
-- but are good practice if you create specific service users later.
GRANT ALL PRIVILEGES ON DATABASE polardb_order TO "user";
GRANT ALL PRIVILEGES ON DATABASE polardb_catalog TO "user";
