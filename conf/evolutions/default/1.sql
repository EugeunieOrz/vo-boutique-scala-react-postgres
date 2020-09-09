# --- !Ups

CREATE TABLE IF NOT EXISTS users (
  user_id VARCHAR NOT NULL PRIMARY KEY,
  title VARCHAR,
  first_name VARCHAR,
  last_name VARCHAR,
  email VARCHAR,
  account_status VARCHAR,
  date_of_birth VARCHAR
);

CREATE TABLE IF NOT EXISTS auth_tokens (
  id CHARACTER VARYING PRIMARY KEY,
  user_id CHARACTER VARYING NOT NULL,
  expiry CHARACTER VARYING NOT NULL
);

CREATE TABLE IF NOT EXISTS login_info (
  user_id CHARACTER VARYING PRIMARY KEY,
  provider_id CHARACTER VARYING NOT NULL,
  provider_key CHARACTER VARYING NOT NULL
);

CREATE TABLE IF NOT EXISTS password_info (
  user_id CHARACTER VARYING PRIMARY KEY,
  password CHARACTER VARYING NOT NULL,
  hasher CHARACTER VARYING NOT NULL,
  salt CHARACTER VARYING
);

CREATE TABLE IF NOT EXISTS user_registration (
  user_id CHARACTER VARYING PRIMARY KEY,
  lang CHARACTER VARYING,
  ip CHARACTER VARYING,
  host CHARACTER VARYING,
  user_agent CHARACTER VARYING,
  date_time CHARACTER VARYING
);

CREATE TABLE IF NOT EXISTS user_settings (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  lang CHARACTER VARYING,
  time_zone CHARACTER VARYING
);

CREATE TABLE IF NOT EXISTS user_age_limit (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  age_limit BOOLEAN DEFAULT FALSE,
  age_limit_condition CHARACTER VARYING
);

CREATE TABLE IF NOT EXISTS user_password_survey (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  reasons VARCHAR[]
);

CREATE TABLE IF NOT EXISTS user_login_attempts (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  attempts JSONB
);

CREATE TABLE IF NOT EXISTS newsletter_task (
  id CHARACTER VARYING NOT NULL PRIMARY KEY,
  email CHARACTER VARYING NOT NULL,
  lang JSONB,
  expiry JSONB,
  newsletter_fashion BOOLEAN DEFAULT FALSE,
  newsletter_fine_jewelry BOOLEAN DEFAULT FALSE,
  newsletter_home_collection BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS newsletter (
  id CHARACTER VARYING NOT NULL PRIMARY KEY,
  email CHARACTER VARYING NOT NULL,
  lang JSONB,
  newsletter_fashion JSONB,
  newsletter_fine_jewelry JSONB,
  newsletter_home_collection JSONB
);

CREATE TABLE IF NOT EXISTS user_newsletter (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  newsletter_fashion JSONB,
  newsletter_fine_jewelry JSONB,
  newsletter_home_collection JSONB
);

CREATE TABLE IF NOT EXISTS user_addresses (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  addresses JSONB
);

CREATE TABLE IF NOT EXISTS user_credit_cards (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  credit_cards JSONB
);

CREATE TABLE IF NOT EXISTS user_wishlist (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  wishlist JSONB
);

CREATE TABLE IF NOT EXISTS user_orders (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  orders JSONB
);

CREATE TABLE IF NOT EXISTS user_shopping_bag (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  shopping_bag JSONB
);

CREATE TABLE IF NOT EXISTS user_last_item_alerts (
  user_id CHARACTER VARYING NOT NULL PRIMARY KEY,
  alerts JSONB
);

# --- !Downs
DROP TABLE user_wishlist;
DROP TABLE user_orders;
DROP TABLE user_shopping_bag;
DROP TABLE user_last_item_alerts;
DROP TABLE user_addresses;
DROP TABLE user_credit_cards;
DROP TABLE user_newsletter;
DROP TABLE newsletter;
DROP TABLE newsletter_task;
DROP TABLE user_password_survey;
DROP TABLE user_login_attempts;
DROP TABLE user_age_limit;
DROP TABLE user_registration;
DROP TABLE user_settings;
DROP TABLE password_info;
DROP TABLE login_info;
DROP TABLE users;
