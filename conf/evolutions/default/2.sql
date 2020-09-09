# --- !Ups
CREATE TABLE IF NOT EXISTS item (
  id UUID PRIMARY KEY,
  name VARCHAR NOT NULL,
  description VARCHAR NOT NULL,
  details VARCHAR[],
  composition JSONB,
  color JSONB,
  size JSONB,
  inventory INT NOT NULL,
  price REAL NOT NULL,
  currency VARCHAR NOT NULL,
  name_of_img INT NOT NULL,
  category VARCHAR NOT NULL,
  sub_category VARCHAR NOT NULL,
  state_of_product VARCHAR NOT NULL,
  department VARCHAR NOT NULL,
  type_of_collection VARCHAR NOT NULL,
  links VARCHAR[],
  availability VARCHAR NOT NULL,
  shipping_costs REAL NOT NULL,
  total REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
  id UUID PRIMARY KEY,
  content JSONB
);

CREATE TABLE IF NOT EXISTS transactions (
  id UUID PRIMARY KEY,
  content JSONB
);

# --- !Downs

DROP TABLE item;
DROP TABLE orders;
DROP TABLE transaction;
