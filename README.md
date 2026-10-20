# WMS - Warehouse Management System

Backend quản lý kho viết bằng Spring Boot. Project tập trung vào business logic nhập hàng, tính nhất quán của tồn kho, transaction và xử lý concurrency, không chỉ dừng ở CRUD.

## Tính năng

- CRUD Category, Product, Warehouse, Supplier (DELETE là xóa mềm, chặn xóa khi còn được dùng)
- Purchase Order: tạo đơn, thêm item, nhận hàng (receive)
- Receive PO cập nhật Inventory và ghi Stock Movement trong cùng một transaction
- Khóa dòng (`SELECT ... FOR UPDATE`) để tránh lost update khi nhiều request cùng cập nhật tồn kho
- Xem tồn kho theo kho / sản phẩm
- Xem lịch sử biến động kho (audit trail)
- Xử lý exception tập trung, trả về HTTP status đúng ngữ nghĩa
- Bean Validation cho mọi request body và query param
- Đăng ký / đăng nhập bằng JWT, phân quyền 2 role: `ADMIN` ghi dữ liệu nền, `STAFF` chỉ đọc và chạy luồng nhập hàng
- Swagger UI tại `/swagger-ui.html`

## Công nghệ

- Java 17
- Spring Boot 4.1.1 (Web, Data JPA, Security, Validation)
- JWT (jjwt 0.12)
- springdoc-openapi (Swagger UI)
- MySQL
- Lombok
- ModelMapper
- Maven (có sẵn Maven Wrapper)

## Yêu cầu

- JDK 17 trở lên
- MySQL 8 đang chạy ở local, hoặc Docker để chạy MySQL bằng `docker-compose.yml` kèm theo

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

   Hoặc dùng Docker, MySQL sẽ tự tạo database `wms` với user `wms` / `wms`:

   ```bash
   docker compose up -d
   ```

3. Tạo file cấu hình kết nối DB và khóa ký JWT

   Copy `src/main/resources/application-uat.example.yaml` thành `src/main/resources/application-uat.yaml` rồi điền username / password MySQL và một chuỗi bí mật Base64 (tạo bằng `openssl rand -base64 64`):

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/wms
       username: <username>
       password: <password>

   jwt:
     secret: <base64-secret>
   ```

   File `application-uat.yaml` đã nằm trong `.gitignore`, không bị commit lên repo. Thời hạn token đặt ở `jwt.expiration` trong `application.yaml` (mặc định `10d`).

4. Chạy ứng dụng

   ```bash
   # Linux / macOS / Git Bash
   ./mvnw spring-boot:run

   # Windows PowerShell / CMD
   mvnw.cmd spring-boot:run
   ```

   Server chạy tại `http://localhost:8081`.

### Lưu ý về dữ liệu

Cấu hình hiện tại dùng `spring.jpa.hibernate.ddl-auto: create`, nghĩa là **mỗi lần khởi động, toàn bộ bảng bị xóa và tạo lại**. Sau đó `data.sql` tự chạy để nạp dữ liệu mẫu (category, product, warehouse, supplier và hai user). Các ID trong dữ liệu mẫu là cố định để Postman collection dùng lại được.

Hai tài khoản có sẵn để thử:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin` | `ADMIN` |
| `staff` | `staff` | `STAFF` |

## Kiểm thử API bằng Postman

Import file `postman/wms.postman_collection.json` vào Postman. Collection gồm:

- Product API: các case thành công và edge case (trùng code, category không tồn tại, ...)
- Category API: CRUD, filter / phân trang, trùng tên 409, xóa category còn product 409
- Auth: register, login, token sai / thiếu / sửa chữ ký -> 401, login kèm token hỏng vẫn 200, `STAFF` ghi dữ liệu nền -> 403, password quá 72 byte -> 400
- Folder `JWT expiry`: chạy riêng khi server dùng token ngắn hạn, ví dụ `mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--jwt.expiration=10s`; token hết hạn -> 401. Với `jwt.expiration: 10d` mặc định thì test này được skip
- Purchase order flow: tạo PO, thêm item, receive, receive lần hai, kiểm tra inventory và stock movement

## Xác thực và phân quyền

Mọi endpoint ngoài `/api/auth/**` và Swagger đều cần header `Authorization: Bearer <accessToken>`. Thiếu hoặc sai token trả 401, đúng token nhưng không đủ quyền trả 403. Cả hai trả body `ErrorResponse` giống các lỗi khác (do `SecurityErrorHandler` ghi, vì lỗi sinh ra trong security filter chain nên `GlobalExceptionHandler` không bắt được).

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/auth/register` | Tạo tài khoản, role luôn là `STAFF` (server không nhận `role` từ client). Username 3 đến 50 ký tự, password 8 ký tự trở lên và tối đa 72 byte UTF-8 |
| POST | `/auth/login` | Trả `{ accessToken, tokenType: "Bearer" }` |

Có 2 role, lưu trong cột `role` của bảng `users`:

| Chức năng | `ADMIN` | `STAFF` |
|---|---|---|
| Đọc category / product / warehouse / supplier, inventory, stock movement | x | x |
| Tạo / sửa / xóa category, product, warehouse, supplier | x | - |
| Tạo purchase order, thêm item, receive | x | x |

Quy tắc nằm trong `WebSecurityConfig`: POST / PUT / DELETE lên 4 nhóm dữ liệu nền yêu cầu `hasRole("ADMIN")`, các request còn lại chỉ cần đăng nhập. Muốn nâng một user lên `ADMIN` thì sửa trực tiếp cột `role` trong DB, chưa có API.

Swagger UI: `http://localhost:8081/swagger-ui.html`, bấm Authorize và dán `accessToken` để gọi thử các endpoint cần token.

## API

Base URL: `http://localhost:8081/api`

### Danh sách, filter và phân trang

Mọi endpoint GET danh sách đều trả về `PageResponse`:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalPages": 3,
  "totalElements": 45
}
```

Query param chung: `page` (bắt đầu từ 0), `size` (mặc định 20), `sort=field,asc|desc` (có thể lặp nhiều lần). `sort` theo field không tồn tại trả về 400. Filter theo id là so khớp chính xác, filter theo text là tìm "chứa", không phân biệt hoa thường.

| Endpoint | Filter | Sort mặc định |
|---|---|---|
| `/categories` | `name`, `description` | `name,asc` |
| `/products` | `name`, `productCode`, `categoryId`, `priceFrom`, `priceTo` | `createdAt,desc` |
| `/warehouses` | `warehouseCode`, `name`, `location` | `createdAt,desc` |
| `/suppliers` | `name`, `phone`, `email`, `address` | `name,asc` |
| `/purchase-orders` | `status` (`PENDING`, `RECEIVED`), `supplierId`, `warehouseId` | `createdAt,desc` |
| `/inventory` | `warehouseId`, `productId` | `updatedAt,desc` |
| `/stock-movements` | `warehouseId`, `productId`, `movementType` (`IN`, `OUT`) | `createdAt,desc` |

Giá trị enum sai (vd `status=FOO`) trả về 400 với `detail` chỉ ra field.

### Category

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/categories` | Danh sách danh mục, có filter và phân trang |
| GET | `/categories/{id}` | Chi tiết danh mục |
| POST | `/categories` | Tạo danh mục, trùng `name` trả về 409 |
| PUT | `/categories/{id}` | Cập nhật danh mục (field null được bỏ qua) |
| DELETE | `/categories/{id}` | Xóa mềm danh mục, trả về 204; còn product (chưa xóa) thuộc danh mục thì 409 |

Product bắt buộc có `categoryId`; category đã xóa mềm không được gán cho product (404 như không tồn tại).

### Product

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/products` | Danh sách sản phẩm, có filter và phân trang (xem mục trên) |
| GET | `/products/{id}` | Chi tiết sản phẩm |
| POST | `/products` | Tạo sản phẩm |
| PUT | `/products/{id}` | Cập nhật sản phẩm (field null được bỏ qua) |
| DELETE | `/products/{id}` | Xóa sản phẩm, trả về 204 |

### Warehouse

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/warehouses` | Danh sách kho, có filter và phân trang |
| GET | `/warehouses/{id}` | Chi tiết kho |
| POST | `/warehouses` | Tạo kho, trùng `warehouseCode` trả về 409 |
| PUT | `/warehouses/{id}` | Cập nhật kho |
| DELETE | `/warehouses/{id}` | Xóa kho, trả về 204 |

### Supplier

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/suppliers` | Danh sách nhà cung cấp, có filter và phân trang |
| GET | `/suppliers/{id}` | Chi tiết nhà cung cấp |
| POST | `/suppliers` | Tạo nhà cung cấp |
| PUT | `/suppliers/{id}` | Cập nhật nhà cung cấp |
| DELETE | `/suppliers/{id}` | Xóa nhà cung cấp, trả về 204 |

### Purchase Order

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/purchase-orders?status=&supplierId=&warehouseId=` | Danh sách đơn nhập, có phân trang |
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

```http
POST /api/purchase-orders/{id}/receive
```

Không có body. Người nhận (`receivedBy`) là user đang đăng nhập, lấy từ token, client không tự gửi được.

### Inventory

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/inventory?warehouseId=&productId=` | Xem tồn kho, có phân trang, cả hai tham số đều không bắt buộc |

### Stock Movement

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/stock-movements?warehouseId=&productId=&movementType=` | Lịch sử biến động, có phân trang, mới nhất trước |
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
- Mọi ID từ client (`supplierId`, `warehouseId`, `productId`) đều được tra lại trong DB, không tồn tại thì trả về 404.
- `receivedBy` không nhận từ client: lấy theo username trong token của người gọi receive.

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
| `ResourceDuplicateException` | 409 | Trùng dữ liệu unique (ví dụ `warehouseCode`, `name` của category) |
| `ResourceInUseException` | 409 | Xóa resource đang được dùng (category còn product, product / warehouse còn tồn kho, supplier / product trong PO `PENDING`) |
| `BadCredentialsException` | 401 | Login sai username hoặc password, message luôn là `Invalid username or password` |
| Thiếu / sai token | 401 | Trả bởi `SecurityErrorHandler` trước khi vào controller, message `Authentication is required` |
| Không đủ quyền | 403 | `STAFF` gọi endpoint chỉ dành cho `ADMIN`, message `You do not have permission to access this resource` |
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
|-- validation     Validation group OnCreate, constraint MaxBytes
|-- enums          PurchaseOrderStatus, MovementType, Role
`-- config         ModelMapper, Spring Security + JwtAuthenticationFilter, OpenAPI
```

Luồng xử lý: `Controller -> Service -> Repository -> Entity`. Business rule nằm ở Service, Controller chỉ nhận request và trả response.

## Chưa có

- API quản lý user và đổi role
- Unit test và integration test
