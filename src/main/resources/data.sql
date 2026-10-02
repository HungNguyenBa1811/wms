-- Seed data for local development.
-- Runs on every startup after Hibernate creates the schema (ddl-auto: create wipes all tables first).
-- IDs are fixed so the Postman collection can reference them.

INSERT INTO categories (id, name, description) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Electronics', 'Electronic devices and accessories'),
    ('22222222-2222-2222-2222-222222222222', 'Furniture', 'Office and home furniture'),
    ('33333333-3333-3333-3333-333333333333', 'Grocery', 'Packaged food and beverages');

INSERT INTO products (id, product_code, name, description, price, category_id, created_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SEED-001', 'Wireless Mouse', 'Seeded product', 15.50,
     '11111111-1111-1111-1111-111111111111', NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SEED-002', 'Office Chair', 'Seeded product', 120.00,
     '22222222-2222-2222-2222-222222222222', NOW());

-- Placeholder user until auth exists; used as receivedBy when receiving a purchase order.
-- Password is not hashed yet.
INSERT INTO users (id, username, email, password, role, created_at) VALUES
    ('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'admin', 'admin@example.com', 'admin', 'ADMIN', NOW());

INSERT INTO warehouses (id, warehouse_code, name, location, created_at) VALUES
    ('c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'WH-001', 'Main Warehouse', 'Ho Chi Minh City', NOW()),
    ('c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'WH-002', 'North Warehouse', 'Ha Noi', NOW());

INSERT INTO suppliers (id, name, phone, email, address) VALUES
    ('d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'ACME Supplies', '0900000001', 'acme@example.com', 'District 1, HCMC'),
    ('d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'Global Trading', '0900000002', 'global@example.com', 'Cau Giay, Ha Noi');
