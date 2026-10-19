-- FairShare PostgreSQL Initialization Script
-- Executed on container startup by Docker entrypoint

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Ensure timezone is set to UTC or Asia/Kolkata as appropriate
SET timezone = 'UTC';
