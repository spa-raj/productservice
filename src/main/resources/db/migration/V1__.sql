CREATE TABLE categories
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    name             VARCHAR(255) NOT NULL,
    `description`    LONGTEXT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE categories_featured_products
(
    category_id          BIGINT NOT NULL,
    featured_products_id BIGINT NOT NULL
);

CREATE TABLE products
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    name             VARCHAR(255) NOT NULL,
    `description`    LONGTEXT NULL,
    image_url        VARCHAR(1000) NULL,
    price DOUBLE NOT NULL,
    category_id      BIGINT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

ALTER TABLE categories_featured_products
    ADD CONSTRAINT uc_categories_featured_products_featuredproducts UNIQUE (featured_products_id);

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name);

ALTER TABLE products
    ADD CONSTRAINT uc_products_name UNIQUE (name);

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE categories_featured_products
    ADD CONSTRAINT fk_catfeapro_on_category FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE categories_featured_products
    ADD CONSTRAINT fk_catfeapro_on_product FOREIGN KEY (featured_products_id) REFERENCES products (id);