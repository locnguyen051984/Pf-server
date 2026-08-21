CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES category(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    origin VARCHAR(100),
    category_id BIGINT NOT NULL REFERENCES category(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE color (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    hex_code VARCHAR(7) NOT NULL
);

CREATE TABLE size (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    description VARCHAR(100)
);

CREATE TABLE product_variant (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    color_id BIGINT NOT NULL REFERENCES color(id),
    size_id BIGINT NOT NULL REFERENCES size(id),
    sku VARCHAR(100) NOT NULL UNIQUE,
    price NUMERIC(12,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

CREATE TABLE product_image (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    image_url TEXT NOT NULL,
    public_id VARCHAR(255),
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_variant_product ON product_variant(product_id);
CREATE INDEX idx_image_product ON product_image(product_id);
CREATE INDEX idx_category_parent ON category(parent_id);