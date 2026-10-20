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

   File mẫu còn có `spring.datasource.hikari.data-source-properties.rewriteBatchedStatements: true`: driver MySQL gộp 1 JDBC batch thành 1 câu INSERT nhiều dòng. Nên giữ, thiếu cờ này thì batch của Hibernate vẫn chạy nhưng driver gửi từng câu một.

   Cấu hình chung trong `application.yaml`:

   - `spring.jpa.open-in-view: false`: mọi query chạy trong transaction của service, không lazy load ở tầng controller / JSON
   - `hibernate.jdbc.batch_size: 50`, `order_inserts`, `order_updates`: INSERT / UPDATE được gom thành JDBC batch khi flush (dùng ở tạo PO và receive)

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

## Chạy test

```bash
./mvnw test
```

- Mặc định chạy trên profile `test` (H2 in-memory, MySQL mode, cấu hình ở `src/test/resources/application-test.yaml`), không cần MySQL và không cần `application-uat.yaml`
- Gồm unit test service (JUnit 5 + Mockito), test controller bằng `@WebMvcTest` (status code, format `ErrorResponse`, 401 / 403) và `contextLoads`
- Test concurrency (`src/test/java/com/hung/wms/concurrency`) gắn `@Tag("mysql")`, cần MySQL thật (row lock InnoDB) nên bị loại khỏi lần chạy mặc định. Chạy riêng khi MySQL đang bật và đã có `application-uat.yaml`:

```bash
./mvnw test -Pmysql-tests
```

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

Quy tắc nằm trong `WebSecurityConfig`: POST / PUT / DELETE lên 4 nhóm dữ liệu nền yêu cầu `hasRole("ADMIN")`, các request còn lại chỉ cần đăng nhập. Muốn nâng một user lên `ADMIN` thì sửa trực tiếp cột `role` trong DB, chưa có API, rồi user đó phải đăng nhập lại (xem dưới).

Role nằm trong token: khi login, server ghi username (`sub`) và role (claim `role`) vào JWT. Mỗi request, `JwtAuthenticationFilter` chỉ verify chữ ký + hạn của token rồi lấy username và role từ đó, **không query bảng `users`**. Đánh đổi:

- Đổi role trong DB chỉ có hiệu lực khi user đăng nhập lại để lấy token mới. Token cũ vẫn mang role cũ cho tới khi hết hạn (`jwt.expiration`, mặc định 10 ngày)
- Tương tự, user bị xóa khỏi DB vẫn dùng được token cũ tới khi hết hạn (hiện chưa có API xóa user)
- Token phát ra trước khi có claim `role` bị coi là không hợp lệ (401), chỉ cần đăng nhập lại

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

Query param chung: `page` (bắt đầu từ 0), `size` (mặc định 20), `sort=field,asc|desc` (có thể lặp nhiều lần). `sort` theo field không tồn tại trả về 400.

Cách so khớp filter:

- Filter theo id, enum, giá: so khớp chính xác / theo khoảng
- `expiryBefore=yyyy-MM-dd` (product): hạn sử dụng **trước** ngày này (không tính ngày đó), dùng để tìm hàng sắp hết hạn. Product không có hạn sử dụng không bao giờ khớp
- `productCode`, `warehouseCode`: tìm theo **tiền tố** (`productCode=SEED` khớp `SEED-001`, `productCode=001` không khớp). Nhờ vậy MySQL dùng được unique index của cột mã
- Các filter text còn lại (`name`, `description`, `location`, `phone`, `email`, `address`): tìm "chứa"
- Không phân biệt hoa thường và không phân biệt dấu (`ha noi` khớp `Hà Nội`): dựa vào collation mặc định của MySQL `utf8mb4_0900_ai_ci`, không dùng `lower()`
- Ký tự `%` và `_` trong giá trị filter được hiểu đúng nghĩa đen, không phải wildcard

| Endpoint | Filter | Sort mặc định |
|---|---|---|
| `/categories` | `name`, `description` | `createdAt,desc` |
| `/products` | `name`, `productCode`, `categoryId`, `priceFrom`, `priceTo`, `expiryBefore` | `createdAt,desc` |
| `/warehouses` | `warehouseCode`, `name`, `location` | `createdAt,desc` |
| `/suppliers` | `name`, `phone`, `email`, `address` | `createdAt,desc` |
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

Ngày nhập và hạn sử dụng (theo feedback mentor, lưu thẳng trên product):

- `importedAt` (`yyyy-MM-dd`, không bắt buộc): ngày nhập. Không gửi khi tạo thì mặc định là hôm nay. Không được ở tương lai (400). Khác `createdAt` (thời điểm tạo bản ghi trong hệ thống)
- `expiryDate` (`yyyy-MM-dd`, không bắt buộc): hạn sử dụng, `null` nghĩa là không hết hạn
- `expiryDate` không được trước `importedAt` -> 400. Gửi cả 2 ngày thì Bean Validation báo lỗi ở `detail` (`expiryDateValid: ...`); PUT chỉ gửi 1 ngày thì service so với ngày đang lưu và trả 400 qua `BadRequestException`
- Giới hạn: hạn sử dụng thật ra thuộc về **lô hàng** (cùng 1 product nhập 2 lần có 2 hạn khác nhau). Ở đây mỗi product chỉ có 1 hạn; tách theo lô (`inventory_lots`, xuất kho FEFO) là hướng B trong CHECKLIST, chưa làm

Ví dụ:

```json
POST /api/products
{
  "productCode": "MILK-1L",
  "name": "Sữa 1L",
  "price": 1.20,
  "categoryId": "...",
  "importedAt": "2026-10-01",
  "expiryDate": "2026-12-31"
}
```

Response có thêm `importedAt`, `expiryDate`, `createdAt`, `createdBy`.

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

### Base entity và người tạo / thời gian tạo

Mọi entity kế thừa `BaseEntity` (`@MappedSuperclass`): `id` (UUID), `createdAt`, `createdBy`, `updatedAt`, `updatedBy`. Dữ liệu nền xóa mềm (Category / Product / Warehouse / Supplier) kế thừa thêm `SoftDeleteEntity` (`isDeleted`).

Các cột audit được điền tự động bằng Spring Data JPA Auditing (`@EnableJpaAuditing` trong `JpaAuditingConfig`, `AuditingEntityListener` trên `BaseEntity`):

- `createdAt` / `updatedAt`: thời điểm insert / update
- `createdBy` / `updatedBy`: **username** của người đang đăng nhập (`SecurityAuditorAware` đọc từ `SecurityContext`, chính là `sub` của JWT). Lưu username (String) thay vì FK tới `users`: không cần JOIN, và hợp với hướng tách service sau này (service khác không có bảng `users`)
- Không có người đăng nhập (đăng ký tài khoản, `data.sql`, test gọi thẳng service) -> `createdBy` = `null`. Không dùng giá trị giả như `"system"` để không trùng với username thật

Response của category, product, warehouse, supplier, purchase order (và item), inventory, stock movement đều có `createdAt` và `createdBy`. `StockMovement.performedBy` (id user, FK) vẫn giữ vì là dữ liệu nghiệp vụ của API; `PurchaseOrder.receivedBy` / `receivedAt` khác nghĩa với `createdBy` (người tạo PO khác người nhận hàng).

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

PO được đọc bằng `PESSIMISTIC_WRITE` lock, nên hai request receive cùng một PO chạy lần lượt và request sau nhận 409.

Receive cộng tồn kho cho mọi item của PO bằng **1 câu upsert** (`InventoryRepositoryCustomImpl.addQuantities`):

```sql
INSERT INTO inventory (id, warehouse_id, product_id, quantity, created_at, created_by, updated_at, updated_by)
VALUES (?, ?, ?, ?, ?, ?, ?, ?), ...
AS incoming
ON DUPLICATE KEY UPDATE quantity = inventory.quantity + incoming.quantity,
                        updated_at = incoming.updated_at, updated_by = incoming.updated_by
```

- Dòng inventory `(warehouse_id, product_id)` chưa có thì được tạo, có rồi thì cộng thêm. MySQL tự kiểm tra unique key `uk_inventory_warehouse_product`: transaction đầu INSERT dòng mới, transaction sau gặp trùng key thì chờ lock của dòng đó rồi cộng vào giá trị mới nhất. Không lost update, không trùng dòng, không lỗi 500.
- Cách cũ (`SELECT ... FOR UPDATE` rồi INSERT nếu chưa có) bị race khi dòng inventory chưa tồn tại: không có dòng để lock, hai transaction cùng INSERT và MySQL báo deadlock (gap lock) hoặc trùng unique.
- Các dòng VALUES được sắp theo `product_id`, nên mọi transaction lock theo cùng thứ tự, tránh deadlock khi hai PO nhiều item cùng chạm các product giống nhau.
- Là native SQL nên `id` (UUID) và các cột audit (`created_at` / `created_by` khi tạo dòng mới, `updated_at` / `updated_by`) được gán trong Java, không qua `@GeneratedValue` / JPA Auditing. Cú pháp `AS incoming` cần MySQL 8.0.19 trở lên (`VALUES(col)` đã deprecated).

Tạo PO lock tất cả product của request bằng 1 query (`id IN (...) ORDER BY id FOR UPDATE`), sắp theo id cùng lý do trên. Sau khi lock xong không còn SELECT nào nữa, nên các INSERT / UPDATE còn lại (stock movement, item, PO) được gửi thành JDBC batch lúc commit.

Test concurrency (`./mvnw test -Pmysql-tests`, cần MySQL thật) chạy nhiều thread receive cùng lúc, cả khi inventory đã có lẫn chưa có, và kiểm tra tồn kho = tổng, đúng 1 dòng inventory, đủ stock movement.

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
|-- controller     Controller (REST endpoint)
|-- service        Interface service
|   `-- impl       Business logic
|-- repository     Spring Data JPA repository (BaseRepository: CRUD + Specification + lock theo id;
|   |              SoftDeleteRepository: thêm findByIdAndIsDeletedFalse[ForUpdate] cho 4 dữ liệu nền)
|   |-- custom     Fragment viết tay (upsert inventory bằng native SQL)
|   `-- specification  SpecificationUtils (contains / startsWith / equal / range / lessThan / notDeleted)
|                  + mỗi entity 1 XxxSpecification.search(request)
|-- entity         JPA entity, BaseEntity (id + audit) / SoftDeleteEntity (isDeleted)
|-- dto
|   |-- request    Request DTO
|   `-- response   Response DTO
|-- mapper         Chuyển đổi Entity <-> DTO (ModelMapper)
|-- exception      Custom exception + GlobalExceptionHandler
|-- validation     Validation group OnCreate, constraint MaxBytes
|-- enums          PurchaseOrderStatus, MovementType, Role
`-- config         ModelMapper, Spring Security + JwtAuthenticationFilter, OpenAPI, JPA Auditing
```

Luồng xử lý: `Controller -> Service -> Repository -> Entity`. Business rule nằm ở Service, Controller chỉ nhận request và trả response.

## Chưa có

- API quản lý user và đổi role
- Integration test chạy không cần MySQL (test concurrency hiện vẫn cần MySQL thật)
