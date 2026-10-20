-- Seed data for local development.
-- Runs on every startup after Hibernate creates the schema (ddl-auto: create wipes all tables first).
-- IDs are fixed so the Postman collection can reference them.
-- Audit columns (BaseEntity): created_at is required; created_by / updated_by hold a username, as JPA auditing would
-- write them (master data by admin, purchase orders by staff). Users have no created_by: they register without login.

INSERT INTO categories (id, name, description, created_at, created_by, updated_at, updated_by) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Electronics', 'Electronic devices and accessories', NOW() - INTERVAL 30 DAY, 'admin', NOW() - INTERVAL 30 DAY, 'admin'),
    ('22222222-2222-2222-2222-222222222222', 'Furniture', 'Office and home furniture', NOW() - INTERVAL 30 DAY, 'admin', NOW() - INTERVAL 30 DAY, 'admin'),
    ('33333333-3333-3333-3333-333333333333', 'Grocery', 'Packaged food and beverages', NOW() - INTERVAL 30 DAY, 'admin', NOW() - INTERVAL 30 DAY, 'admin');

-- Seeded products do not expire (expiry_date NULL); Postman creates one with an expiry date to test the filter.
INSERT INTO products (id, product_code, name, description, price, category_id, imported_at, created_at, created_by, updated_at, updated_by) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SEED-001', 'Wireless Mouse', 'Seeded product', 15.50,
     '11111111-1111-1111-1111-111111111111', CURDATE() - INTERVAL 30 DAY, NOW(), 'admin', NOW(), 'admin'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SEED-002', 'Office Chair', 'Seeded product', 120.00,
     '22222222-2222-2222-2222-222222222222', CURDATE() - INTERVAL 30 DAY, NOW(), 'admin', NOW(), 'admin');

-- receivedBy is the logged-in user (taken from the JWT), not sent by the client.
-- Passwords are BCrypt hashes of the username: login with admin / admin or staff / staff.
-- ADMIN writes master data; STAFF can only read it and run the purchase order flow.
INSERT INTO users (id, username, email, password, role, created_at, updated_at) VALUES
    ('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'admin', 'admin@example.com',
     '$2a$10$I9Z5kT14idFN9DUvRxu5f.kGwli1p4sWOB5PmY0P6KS3HrFqt8MBG', 'ADMIN', NOW(), NOW()),
    ('e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'staff', 'staff@example.com',
     '$2a$10$5WUCga7f9S1O9NW9wTfKku5wS9w2KNG8lBzinuZJSTOK94722oO7i', 'STAFF', NOW(), NOW());

INSERT INTO warehouses (id, warehouse_code, name, location, created_at, created_by, updated_at, updated_by) VALUES
    ('c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'WH-001', 'Main Warehouse', 'Ho Chi Minh City', NOW(), 'admin', NOW(), 'admin'),
    ('c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'WH-002', 'North Warehouse', 'Ha Noi', NOW(), 'admin', NOW(), 'admin');

INSERT INTO suppliers (id, name, phone, email, address, created_at, created_by, updated_at, updated_by) VALUES
    ('d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'ACME Supplies', '0900000001', 'acme@example.com', 'District 1, HCMC', NOW() - INTERVAL 30 DAY, 'admin', NOW() - INTERVAL 30 DAY, 'admin'),
    ('d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'Global Trading', '0900000002', 'global@example.com', 'Cau Giay, Ha Noi', NOW() - INTERVAL 30 DAY, 'admin', NOW() - INTERVAL 30 DAY, 'admin');

-- Purchase orders for testing list queries (N+1, paging, filters).
-- 12 POs, 8 RECEIVED (by admin and staff) + 4 PENDING, created 12 to 1 days ago so Postman-created POs stay newest.
-- Inventory and stock movements below match the RECEIVED POs exactly, as if each had gone through /receive.
INSERT INTO purchase_orders (id, supplier_id, warehouse_id, status, created_at, received_at, received_by_id, created_by, updated_at, updated_by) VALUES
    ('f1f1f1f1-0000-0000-0000-000000000001', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'RECEIVED', NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 11 DAY, 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'staff', NOW() - INTERVAL 11 DAY, 'admin'),
    ('f1f1f1f1-0000-0000-0000-000000000002', 'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'RECEIVED', NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 10 DAY, 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'staff', NOW() - INTERVAL 10 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000003', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'RECEIVED', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY, 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'staff', NOW() - INTERVAL 9 DAY, 'admin'),
    ('f1f1f1f1-0000-0000-0000-000000000004', 'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'RECEIVED', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY, 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'staff', NOW() - INTERVAL 8 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000005', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'RECEIVED', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 7 DAY, 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'staff', NOW() - INTERVAL 7 DAY, 'admin'),
    ('f1f1f1f1-0000-0000-0000-000000000006', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'RECEIVED', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY, 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'staff', NOW() - INTERVAL 6 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000007', 'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'RECEIVED', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 5 DAY, 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'staff', NOW() - INTERVAL 5 DAY, 'admin'),
    ('f1f1f1f1-0000-0000-0000-000000000008', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'RECEIVED', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY, 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'staff', NOW() - INTERVAL 4 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000009', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'PENDING', NOW() - INTERVAL 4 DAY, NULL, NULL, 'staff', NOW() - INTERVAL 4 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000010', 'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'PENDING', NOW() - INTERVAL 3 DAY, NULL, NULL, 'staff', NOW() - INTERVAL 3 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000011', 'd1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'PENDING', NOW() - INTERVAL 2 DAY, NULL, NULL, 'staff', NOW() - INTERVAL 2 DAY, 'staff'),
    ('f1f1f1f1-0000-0000-0000-000000000012', 'd2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'PENDING', NOW() - INTERVAL 1 DAY, NULL, NULL, 'staff', NOW() - INTERVAL 1 DAY, 'staff');

INSERT INTO purchase_order_items (id, purchase_order_id, product_id, quantity, unit_cost, created_at, created_by, updated_at, updated_by) VALUES
    ('f2f2f2f2-0000-0000-0000-000000000001', 'f1f1f1f1-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 50, 10.00, NOW() - INTERVAL 12 DAY, 'staff', NOW() - INTERVAL 12 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000002', 'f1f1f1f1-0000-0000-0000-000000000001', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 10, 80.00, NOW() - INTERVAL 12 DAY, 'staff', NOW() - INTERVAL 12 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000003', 'f1f1f1f1-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 30, 9.50, NOW() - INTERVAL 11 DAY, 'staff', NOW() - INTERVAL 11 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000004', 'f1f1f1f1-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 5, 82.00, NOW() - INTERVAL 10 DAY, 'staff', NOW() - INTERVAL 10 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000005', 'f1f1f1f1-0000-0000-0000-000000000004', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 20, 10.20, NOW() - INTERVAL 9 DAY, 'staff', NOW() - INTERVAL 9 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000006', 'f1f1f1f1-0000-0000-0000-000000000004', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 4, 79.00, NOW() - INTERVAL 9 DAY, 'staff', NOW() - INTERVAL 9 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000007', 'f1f1f1f1-0000-0000-0000-000000000005', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 6, 81.00, NOW() - INTERVAL 8 DAY, 'staff', NOW() - INTERVAL 8 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000008', 'f1f1f1f1-0000-0000-0000-000000000006', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 40, 9.80, NOW() - INTERVAL 7 DAY, 'staff', NOW() - INTERVAL 7 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000009', 'f1f1f1f1-0000-0000-0000-000000000007', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 25, 10.00, NOW() - INTERVAL 6 DAY, 'staff', NOW() - INTERVAL 6 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000010', 'f1f1f1f1-0000-0000-0000-000000000007', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 3, 80.50, NOW() - INTERVAL 6 DAY, 'staff', NOW() - INTERVAL 6 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000011', 'f1f1f1f1-0000-0000-0000-000000000008', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 15, 9.90, NOW() - INTERVAL 5 DAY, 'staff', NOW() - INTERVAL 5 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000012', 'f1f1f1f1-0000-0000-0000-000000000009', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 10, 10.00, NOW() - INTERVAL 4 DAY, 'staff', NOW() - INTERVAL 4 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000013', 'f1f1f1f1-0000-0000-0000-000000000009', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 2, 80.00, NOW() - INTERVAL 4 DAY, 'staff', NOW() - INTERVAL 4 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000014', 'f1f1f1f1-0000-0000-0000-000000000010', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 8, 78.00, NOW() - INTERVAL 3 DAY, 'staff', NOW() - INTERVAL 3 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000015', 'f1f1f1f1-0000-0000-0000-000000000011', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 60, 9.70, NOW() - INTERVAL 2 DAY, 'staff', NOW() - INTERVAL 2 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000016', 'f1f1f1f1-0000-0000-0000-000000000012', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 12, 10.10, NOW() - INTERVAL 1 DAY, 'staff', NOW() - INTERVAL 1 DAY, 'staff'),
    ('f2f2f2f2-0000-0000-0000-000000000017', 'f1f1f1f1-0000-0000-0000-000000000012', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 1, 82.00, NOW() - INTERVAL 1 DAY, 'staff', NOW() - INTERVAL 1 DAY, 'staff');

INSERT INTO inventory (id, warehouse_id, product_id, quantity, updated_at, created_at, created_by, updated_by) VALUES
    ('f4f4f4f4-0000-0000-0000-000000000001', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 120, NOW(), NOW() - INTERVAL 11 DAY, 'admin', 'staff'),
    ('f4f4f4f4-0000-0000-0000-000000000002', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 19, NOW(), NOW() - INTERVAL 11 DAY, 'admin', 'staff'),
    ('f4f4f4f4-0000-0000-0000-000000000003', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 60, NOW(), NOW() - INTERVAL 11 DAY, 'admin', 'staff'),
    ('f4f4f4f4-0000-0000-0000-000000000004', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 9, NOW(), NOW() - INTERVAL 11 DAY, 'admin', 'staff');

INSERT INTO stock_movements (id, product_id, warehouse_id, performed_by, movement_type, quantity, reason, created_at, created_by, updated_at, updated_by) VALUES
    ('f3f3f3f3-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'IN', 50, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000001', NOW() - INTERVAL 11 DAY, 'admin', NOW() - INTERVAL 11 DAY, 'admin'),
    ('f3f3f3f3-0000-0000-0000-000000000002', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'IN', 10, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000001', NOW() - INTERVAL 11 DAY, 'admin', NOW() - INTERVAL 11 DAY, 'admin'),
    ('f3f3f3f3-0000-0000-0000-000000000003', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'IN', 30, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000002', NOW() - INTERVAL 10 DAY, 'staff', NOW() - INTERVAL 10 DAY, 'staff'),
    ('f3f3f3f3-0000-0000-0000-000000000004', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'IN', 5, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000003', NOW() - INTERVAL 9 DAY, 'admin', NOW() - INTERVAL 9 DAY, 'admin'),
    ('f3f3f3f3-0000-0000-0000-000000000005', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'IN', 20, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000004', NOW() - INTERVAL 8 DAY, 'staff', NOW() - INTERVAL 8 DAY, 'staff'),
    ('f3f3f3f3-0000-0000-0000-000000000006', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'IN', 4, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000004', NOW() - INTERVAL 8 DAY, 'staff', NOW() - INTERVAL 8 DAY, 'staff'),
    ('f3f3f3f3-0000-0000-0000-000000000007', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'IN', 6, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000005', NOW() - INTERVAL 7 DAY, 'admin', NOW() - INTERVAL 7 DAY, 'admin'),
    ('f3f3f3f3-0000-0000-0000-000000000008', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'IN', 40, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000006', NOW() - INTERVAL 6 DAY, 'staff', NOW() - INTERVAL 6 DAY, 'staff'),
    ('f3f3f3f3-0000-0000-0000-000000000009', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'IN', 25, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000007', NOW() - INTERVAL 5 DAY, 'admin', NOW() - INTERVAL 5 DAY, 'admin'),
    ('f3f3f3f3-0000-0000-0000-000000000010', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'IN', 3, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000007', NOW() - INTERVAL 5 DAY, 'admin', NOW() - INTERVAL 5 DAY, 'admin'),
    ('f3f3f3f3-0000-0000-0000-000000000011', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'e2e2e2e2-e2e2-e2e2-e2e2-e2e2e2e2e2e2', 'IN', 15, 'Receive purchase order f1f1f1f1-0000-0000-0000-000000000008', NOW() - INTERVAL 4 DAY, 'staff', NOW() - INTERVAL 4 DAY, 'staff');
