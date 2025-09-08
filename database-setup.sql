-- Database setup script for Phenikaa Thesis Management System
-- This script creates all required databases for the microservices

-- Create databases for each service
CREATE DATABASE user_db;
CREATE DATABASE thesis_db;
CREATE DATABASE profile_db;
CREATE DATABASE group_db;
CREATE DATABASE assign_db;
CREATE DATABASE submission_db;
CREATE DATABASE academic_db;
CREATE DATABASE evaluation_db;

-- Create user for the application
CREATE USER phenikaa_user WITH PASSWORD 'phenikaa_password';

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE user_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE thesis_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE profile_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE group_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE assign_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE submission_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE academic_db TO phenikaa_user;
GRANT ALL PRIVILEGES ON DATABASE evaluation_db TO phenikaa_user;

-- Grant schema privileges
\c user_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c thesis_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c profile_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c group_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c assign_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c submission_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c academic_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

\c evaluation_db;
GRANT ALL ON SCHEMA public TO phenikaa_user;

-- Display created databases
SELECT datname FROM pg_database WHERE datname LIKE '%_db';
