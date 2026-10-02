# WMS - Warehouse Management System

Backend quản lý kho viết bằng Spring Boot. Project tập trung vào business logic nhập hàng, tính nhất quán của tồn kho, transaction và xử lý concurrency, không chỉ dừng ở CRUD.

## Tính năng

- CRUD Product, Warehouse, Supplier
- Purchase Order: tạo đơn, thêm item, nhận hàng (receive)
- Receive PO cập nhật Inventory và ghi Stock Movement trong cùng một transaction
- Khóa dòng (`SELECT ... FOR UPDATE`) để tránh lost update khi nhiều request cùng cập nhật tồn kho
- Xem tồn kho theo kho / sản phẩm
- Xem lịch sử biến động kho (audit trail)
- Xử lý exception tập trung, trả về HTTP status đúng ngữ nghĩa

## Công nghệ

- Java 17
- Spring Boot 4.1.1 (Web, Data JPA)
- MySQL
- Lombok
- ModelMapper
- Maven (có sẵn Maven Wrapper)

## Yêu cầu

- JDK 17 trở lên
- MySQL 8 đang chạy ở local

## Cài đặt và chạy

1. Clone project

   ```bash
   git clone https://github.com/HungNguyenBa1811/wms.git
   cd wms
   ```

2. Tạo database

   ```sql
   CREATE DATABASE wms;
   ```

3. Tạo file cấu hình kết nối DB

   Copy `src/main/resources/application-uat.example.yaml` thành `src/main/resources/application-uat.yaml` rồi điền username / password MySQL:

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/wms
       username: <username>
       password: <password>
   ```

   File `application-uat.yaml` đã nằm trong `.gitignore`, không bị commit lên repo.

4. Chạy ứng dụng

   ```bash
   # Linux / macOS / Git Bash
   ./mvnw spring-boot:run

   # Windows PowerShell / CMD
   mvnw.cmd spring-boot:run
   ```

   Server chạy tại `http://localhost:8081`.

### Lưu ý về dữ liệu

Cấu hình hiện tại dùng `spring.jpa.hibernate.ddl-auto: create`, nghĩa là **mỗi lần khởi động, toàn bộ bảng bị xóa và tạo lại**. Sau đó `data.sql` tự chạy để nạp dữ liệu mẫu (category, product, warehouse, supplier và một user `admin`). Các ID trong dữ liệu mẫu là cố định để Postman collection dùng lại được.

## Kiểm thử API bằng Postman

Import file `postman/wms.postman_collection.json` vào Postman. Collection gồm:

- Product API: các case thành công và edge case (trùng code, category không tồn tại, ...)
- Purchase order flow: tạo PO, thêm item, receive, receive lần hai, kiểm tra inventory và stock movement

## API

Base URL: `http://localhost:8081/api`

### Product

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/products` | Danh sách sản phẩm |
| GET | `/products/{id}` | Chi tiết sản phẩm |
| POST | `/products` | Tạo sản phẩm |
| PUT | `/products/{id}` | Cập nhật sản phẩm (field null được bỏ qua) |
| DELETE | `/products/{id}` | Xóa sản phẩm, trả về 204 |

### Warehouse

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/warehouses` | Danh sách kho |
| GET | `/warehouses/{id}` | Chi tiết kho |
| POST | `/warehouses` | Tạo kho, trùng `warehouseCode` trả về 409 |
| PUT | `/warehouses/{id}` | Cập nhật kho |
| DELETE | `/warehouses/{id}` | Xóa kho, trả về 204 |

### Supplier

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/suppliers` | Danh sách nhà cung cấp |
| GET | `/suppliers/{id}` | Chi tiết nhà cung cấp |
| POST | `/suppliers` | Tạo nhà cung cấp |
| PUT | `/suppliers/{id}` | Cập nhật nhà cung cấp |
| DELETE | `/suppliers/{id}` | Xóa nhà cung cấp, trả về 204 |

### Purchase Order

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/purchase-orders` | Danh sách đơn nhập |
| GET | `/purchase-orders/{id}` | Chi tiết đơn nhập |
| POST | `/purchase-orders` | Tạo đơn nhập (có thể kèm items) |
| POST | `/purchase-orders/{id}/items` | Thêm item vào đơn |
| POST | `/purchase-orders/{id}/receive` | Nhận hàng, cập nhật tồn kho |

Tạo đơn:

```json
POST /api/purchase-orders
{
  "supplierId": "...",
  "warehouseId": "...",
  "items": [
    { "productId": "...", "quantity": 100, "unitCost": 500 }
  ]
}
```

Thêm item:

```json
POST /api/purchase-orders/{id}/items
{
  "productId": "...",
  "quantity": 100,
  "unitCost": 500
}
```

Nhận hàng:

```json
POST /api/purchase-orders/{id}/receive
{
  "receivedBy": "<userId>"
}
```

### Inventory

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/inventory?warehouseId=&productId=` | Xem tồn kho, cả hai tham số đều không bắt buộc |

### Stock Movement

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/stock-movements?warehouseId=&productId=` | Lịch sử biến động, mới nhất trước |
| GET | `/stock-movements/{id}` | Chi tiết một biến động |

Inventory và Stock Movement **không có API ghi**. Đây là dữ liệu phát sinh từ nghiệp vụ (receive PO). Nếu cho phép client tự tạo stock movement thì client có thể tự tăng tồn kho tùy ý.

## Business rules

### Vòng đời Purchase Order

```text
PENDING
   |
   +-- Add item
   |
   +-- Receive
         |
         +-- Cộng Inventory
         +-- Tạo Stock Movement (IN)
         +-- Chuyển sang RECEIVED
```

- Chỉ PO ở trạng thái `PENDING` mới được thêm item và receive.
- PO đã `RECEIVED` không thể thêm item hay receive lại (409).
- PO phải có ít nhất một item mới được receive (400).
- `quantity` phải lớn hơn 0, `unitCost` không được âm (400).
- Mọi ID từ client (`supplierId`, `warehouseId`, `productId`, `receivedBy`) đều được tra lại trong DB, không tồn tại thì trả về 404.

### Transaction và concurrency

Receive PO thay đổi nhiều bảng (inventory, stock_movements, purchase_orders), nên toàn bộ chạy trong một `@Transactional`: thành công hết hoặc rollback hết.

PO và Inventory được đọc bằng `PESSIMISTIC_WRITE` lock. Khi hai request cùng cộng tồn kho cho một sản phẩm trong một kho, request sau phải chờ request trước commit, tránh trường hợp cả hai cùng đọc giá trị cũ rồi ghi đè lên nhau.

## Xử lý lỗi

Mọi lỗi nghiệp vụ được `GlobalExceptionHandler` chuyển thành response thống nhất:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot receive purchase order in status: RECEIVED",
  "path": "/api/purchase-orders/.../receive"
}
```

| Exception | HTTP status | Khi nào |
|---|---|---|
| `ResourceNotFoundException` | 404 | Resource được tham chiếu không tồn tại |
| `BadRequestException` | 400 | Dữ liệu đầu vào không hợp lệ |
| `InvalidStateException` | 409 | Trạng thái hiện tại không cho phép thao tác |
| `ResourceDuplicateException` | 409 | Trùng dữ liệu unique (ví dụ `warehouseCode`) |
| Lỗi khác | 500 | Lỗi không lường trước, có ghi log |

## Cấu trúc project

```text
src/main/java/com/hung/wms
|-- api            Controller (REST endpoint)
|-- service        Interface service
|   `-- impl       Business logic
|-- repository     Spring Data JPA repository
|   `-- entity     JPA entity
|-- model
|   |-- request    Request DTO
|   `-- response   Response DTO
|-- converter      Chuyển đổi Entity <-> DTO (ModelMapper)
|-- exception      Custom exception + GlobalExceptionHandler
|-- enums          PurchaseOrderStatus, MovementType
`-- config         Cấu hình ModelMapper
```

Luồng xử lý: `Controller -> Service -> Repository -> Entity`. Business rule nằm ở Service, Controller chỉ nhận request và trả response.

## Chưa có

- Đăng nhập / đăng ký (Auth, JWT)
- Bean Validation (`@Valid`)
- Unit test và integration test
- Phân trang, filter cho các API danh sách
