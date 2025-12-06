-- Drop old tables
DROP TABLE IF EXISTS product_variants CASCADE;
DROP TABLE IF EXISTS products CASCADE;

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    vendor VARCHAR(50) NOT NULL,
    product_type VARCHAR(50) --can be null since some products don't have product_type
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    color_option VARCHAR(30) NOT NULL,
    size_option VARCHAR(50), --can be null since some products don't have sizes
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN DEFAULT true
);


