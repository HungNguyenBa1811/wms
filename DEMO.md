# WMS - Tài liệu chuẩn bị demo với mentor

> Doc này tôi viết để tự ôn trước buổi demo, và cũng để bạn (mentor, hoặc bất kỳ ai đọc) nắm được project đang làm gì mà không cần mở code. Giọng văn casual, tôi kể như đang nói chuyện. Phần kỹ thuật sâu hơn nằm ở `README.md`.

---

## 0. Bài toán tôi đang giải

Tưởng tượng bạn có một cái kho (hoặc vài cái kho). Hàng từ nhà cung cấp chở về, bạn nhận vào kho. Câu hỏi muôn thuở: **"giờ trong kho đang có bao nhiêu cái gì?"** và **"con số đó từ đâu ra, ai nhập, lúc nào?"**

Project này là phần backend (API) trả lời hai câu hỏi đó. Không có giao diện, test bằng Postman hoặc Swagger.

Cái tôi muốn nhấn mạnh với bạn: project **không chỉ là CRUD**. Phần nặng nhất là làm sao con số tồn trong kho **luôn đúng**, kể cả khi hai người bấm "nhận hàng" cùng một lúc, hoặc khi đang nhận hàng thì server lỗi giữa chừng.

---

## 1. Từ điển: giải thích một lần, sau đó tôi nói tiếng Việt

Dưới đây là các thuật ngữ trong code. Tôi giải thích ở đây một lần, **từ phần 2 trở đi tôi sẽ dùng cách diễn giải tiếng Việt** thay cho thuật ngữ, để đọc đỡ mệt.

| Thuật ngữ trong code | Tôi sẽ gọi là | Nghĩa là gì |
|---|---|---|
| Product | sản phẩm | Một mặt hàng. Có mã (`productCode`), tên, giá, thuộc một danh mục. |
| Category | danh mục | Nhóm sản phẩm (ví dụ "Đồ uống", "Văn phòng phẩm"). Sản phẩm bắt buộc phải thuộc một danh mục. |
| Warehouse | kho | Một địa điểm chứa hàng. Có mã kho, tên, vị trí. |
| Supplier | nhà cung cấp | Bên bán hàng cho mình. |
| Purchase Order (PO) | đơn nhập hàng | Tờ giấy ghi "tôi mua từ nhà cung cấp X, chở về kho Y, gồm các mặt hàng sau". |
| Purchase Order Item | dòng hàng trong đơn | Một dòng trong đơn nhập: sản phẩm nào, bao nhiêu cái, giá vốn mỗi cái. |
| Receive (PO) | nhận hàng | Hành động xác nhận "hàng đã về kho". Đây là lúc tồn kho thay đổi. |
| Inventory | tồn kho | Con số "kho Y đang có N cái sản phẩm X". Mỗi cặp (kho, sản phẩm) là một dòng. |
| Stock Movement | nhật ký kho | Mỗi lần tồn kho thay đổi thì ghi một dòng: sản phẩm gì, kho nào, tăng hay giảm, bao nhiêu, ai làm, lý do. Giống sổ cái kế toán. |
| Status PENDING / RECEIVED | đang chờ / đã nhận | Hai trạng thái của đơn nhập. Đang chờ = hàng chưa về. Đã nhận = hàng đã vào kho, đơn khóa lại. |
| User | người dùng | Tài khoản đăng nhập. Ai nhận hàng thì ghi tên vào nhật ký kho. |
| JWT (token) | vé vào cửa | Đăng nhập xong được cấp một cái vé. Mọi request sau phải kèm vé, không có vé thì bị từ chối. |
| Soft delete | xóa mềm | Không xóa thật khỏi database, chỉ đánh dấu "đã xóa". Dữ liệu cũ (đơn nhập, nhật ký) vẫn tham chiếu được. |
| Transaction | gói thao tác | Nhóm nhiều bước ghi database thành một gói: hoặc tất cả thành công, hoặc tất cả hủy. Không có chuyện xong một nửa. |
| Row lock (`SELECT ... FOR UPDATE`) | khóa dòng | Khi một request đang sửa một dòng dữ liệu, request khác muốn sửa cùng dòng đó phải xếp hàng chờ. |
| Specification / Pageable | lọc và phân trang | Cách lấy danh sách có điều kiện lọc, có chia trang, có sắp xếp. |

OK, từ đây trở đi: không "inventory", chỉ "tồn kho". Không "PO", chỉ "đơn nhập hàng".

---

## 2. Bức tranh tổng thể

Các khối dữ liệu và chúng liên quan với nhau thế nào:

```text
        +-----------+          +-----------+
        | danh mục  | 1 ---- N | sản phẩm  |
        +-----------+          +-----+-----+
                                     |
            +------------------------+-------------------------+
            |                        |                         |
            N                        N                         N
   +--------+---------+   +----------+----------+   +----------+----------+
   | dòng hàng trong  |   |      tồn kho        |   |     nhật ký kho     |
   | đơn nhập         |   | (kho + sản phẩm     |   | (kho, sản phẩm,     |
   +--------+---------+   |  -> số lượng)       |   |  +/- bao nhiêu, ai) |
            N             +----------+----------+   +----------+----------+
            |                        |                         |
            1                        N                         N
   +--------+---------+              |                         |
   | đơn nhập hàng    |              |                         |
   | - nhà cung cấp   |   +----------+----------+              |
   | - kho  ----------+---|        kho          |--------------+
   | - trạng thái     |   +---------------------+
   | - ai nhận, lúc   |
   |   nào            |              +---------------------+
   +--------+---------+              |     người dùng      |
            |                        | (ai nhận hàng, ai   |
            +------------------------| ghi nhật ký)        |
                                     +---------------------+
            N
            |
            1
   +--------+---------+
   |  nhà cung cấp    |
   +------------------+
```

Đọc sơ đồ:

- Một đơn nhập hàng gắn với **một** nhà cung cấp và **một** kho, chứa **nhiều** dòng hàng.
- Tồn kho là bảng "kho x sản phẩm = số lượng". Có ràng buộc unique trên cặp (kho, sản phẩm), nên không bao giờ có hai dòng tồn cho cùng một sản phẩm trong cùng một kho.
- Nhật ký kho chỉ **ghi thêm**, không bao giờ sửa hay xóa. Muốn biết tại sao tồn kho là 150 thì đọc nhật ký cộng lại.

---

## 3. Luồng nghiệp vụ, kể theo thứ tự thực tế

### 3.1 Bước 0: đăng nhập lấy vé

Mọi API ngoại trừ đăng ký / đăng nhập (và trang Swagger) đều yêu cầu vé.

```text
   bạn                              server
    |                                 |
    |  POST /api/auth/register        |
    |  {username, email, password}    |
    |-------------------------------->|  kiểm tra trùng username / email
    |                                 |  mã hóa mật khẩu (BCrypt)
    |                                 |  lưu, role mặc định STAFF
    |<--------------------------------|  201 + thông tin user
    |                                 |
    |  POST /api/auth/login           |
    |  {username, password}           |
    |-------------------------------->|  so mật khẩu
    |<--------------------------------|  200 + { accessToken: "eyJ..." }
    |                                 |
    |  GET /api/products              |
    |  Authorization: Bearer eyJ...   |
    |-------------------------------->|  bộ lọc đọc vé, kiểm tra chữ ký
    |                                 |  và hạn, gắn user vào request
    |<--------------------------------|  200 + danh sách
    |                                 |
    |  GET /api/products  (quên vé)   |
    |-------------------------------->|
    |<--------------------------------|  401
```

Vé hiện có hạn 10 ngày (cấu hình được). Vé sai chữ ký, hết hạn, hoặc user trong vé không còn tồn tại thì bị từ chối.

Có hai loại vé, ghi trong cột `role` của user:

- `ADMIN`: làm được mọi thứ, gồm tạo / sửa / xóa dữ liệu nền (danh mục, sản phẩm, kho, nhà cung cấp).
- `STAFF`: chỉ đọc dữ liệu nền, nhưng chạy được toàn bộ luồng nhập hàng (tạo đơn, thêm dòng, nhận hàng). Đăng ký mới luôn là `STAFF`.

STAFF cố ghi dữ liệu nền thì nhận 403. Quy tắc nằm gọn trong một chỗ (`WebSecurityConfig`): POST / PUT / DELETE lên 4 nhóm dữ liệu nền cần `ADMIN`, còn lại chỉ cần có vé.

Dữ liệu mẫu có sẵn `admin` / `admin` (ADMIN) và `staff` / `staff` (STAFF) để demo nhanh.

### 3.2 Bước 1: dựng dữ liệu nền

Trước khi nhập hàng, phải có thứ để nhập và chỗ để nhập vào:

```text
   tạo danh mục  -->  tạo sản phẩm (phải chỉ danh mục)
   tạo kho
   tạo nhà cung cấp
```

Bốn thứ này đều là thêm / sửa / xem / xóa bình thường. Có vài điểm tôi muốn bạn chú ý:

- **Trùng thì từ chối (409)**: trùng tên danh mục, trùng mã sản phẩm, trùng mã kho.
- **Tham chiếu phải tồn tại (404)**: tạo sản phẩm với danh mục không có thật, hoặc danh mục đã xóa mềm, thì báo không tìm thấy. Tôi không tin bất kỳ ID nào client gửi lên, cái gì cũng tra lại database.
- **Xem danh sách có lọc + phân trang + sắp xếp**: ví dụ sản phẩm lọc theo tên, mã, danh mục, khoảng giá. Sắp xếp theo field không tồn tại thì báo 400 thay vì nổ 500.
- **Sửa thì field nào null bỏ qua**, chỉ cập nhật field gửi lên.

### 3.3 Bước 2: nhập hàng, luồng chính của project

Đây là phần quan trọng nhất. Đơn nhập hàng chỉ có hai trạng thái:

```text
   +-------------------+                          +-------------------+
   |     ĐANG CHỜ      |      nhận hàng           |      ĐÃ NHẬN      |
   |     (PENDING)     | -----------------------> |    (RECEIVED)     |
   +-------------------+                          +-------------------+
     |            ^                                  không làm gì
     |  thêm dòng |                                  được nữa:
     +------------+                                  - thêm dòng -> 409
                                                     - nhận lại  -> 409
```

Luồng đầy đủ từ lúc tạo đơn đến lúc hàng vào kho:

```text
   bạn                                      server
    |                                         |
    |  POST /api/purchase-orders              |
    |  { nhà cung cấp, kho,                   |
    |    items: [ {sản phẩm, 100 cái,         |
    |              giá vốn 500} ] }           |
    |---------------------------------------->|  tra nhà cung cấp, kho,
    |                                         |  từng sản phẩm trong DB
    |                                         |  (thiếu cái nào -> 404)
    |                                         |  số lượng <= 0 hay giá âm -> 400
    |                                         |  cùng sản phẩm hai dòng -> 400
    |                                         |  trạng thái = ĐANG CHỜ
    |<----------------------------------------|  201 + đơn
    |                                         |
    |  POST /api/purchase-orders/{id}/items   |
    |  { sản phẩm khác, 50 cái, giá 200 }     |
    |---------------------------------------->|  khóa dòng đơn
    |                                         |  đơn phải ĐANG CHỜ, không -> 409
    |                                         |  thêm dòng
    |<----------------------------------------|  200 + đơn (2 dòng)
    |                                         |
    |  POST /api/purchase-orders/{id}/receive |
    |  { receivedBy: <id người nhận> }        |
    |---------------------------------------->|  ===== bắt đầu một gói =====
    |                                         |  khóa dòng đơn
    |                                         |  đơn phải ĐANG CHỜ          -> 409
    |                                         |  đơn phải có ít nhất 1 dòng -> 400
    |                                         |  người nhận phải tồn tại    -> 404
    |                                         |  với từng dòng hàng:
    |                                         |    khóa dòng tồn kho (kho, sp)
    |                                         |    chưa có thì tạo mới = 0
    |                                         |    tồn kho += số lượng
    |                                         |    ghi 1 dòng nhật ký (+N, ai, lý do)
    |                                         |  đơn -> ĐÃ NHẬN, lưu ai nhận, lúc nào
    |                                         |  ===== kết thúc gói, commit =====
    |<----------------------------------------|  200 + đơn (status RECEIVED)
    |                                         |
    |  POST .../receive  (bấm lần 2)          |
    |---------------------------------------->|  đơn đã ĐÃ NHẬN
    |<----------------------------------------|  409 "Cannot receive purchase order
    |                                         |       in status: RECEIVED"
```

Điểm tôi muốn bạn thấy: **tồn kho và nhật ký kho không có API ghi**. Client không thể gọi `POST /inventory` để tự cộng số. Cách duy nhất để tồn kho thay đổi là qua nghiệp vụ nhận hàng. Nếu mở API ghi thì ai cũng có thể sửa số tồn tùy ý, và nhật ký mất ý nghĩa.

### 3.4 Bước 3: xem kết quả

Sau khi nhận hàng, bạn kiểm tra hai chỗ:

```text
   GET /api/inventory?warehouseId=...&productId=...
   -> kho Y, sản phẩm X, số lượng 100, cập nhật lúc ...

   GET /api/stock-movements?warehouseId=...&productId=...
   -> [ { IN, +100, người nhận: admin, lý do: "Receive purchase order <id>", lúc ... } ]
```

Cả hai đều có lọc và phân trang. Nhật ký sắp xếp mới nhất trước.

### 3.5 Xóa: xóa mềm và chặn xóa khi còn dùng

Xóa danh mục, sản phẩm, kho, nhà cung cấp đều là **xóa mềm**: đánh dấu `isDeleted = true`, lọc danh sách không hiện nữa, nhưng dòng vẫn nằm trong database để đơn nhập cũ và nhật ký cũ còn tham chiếu được.

Và không phải cứ muốn xóa là được:

```text
   xóa danh mục ------ còn sản phẩm (chưa xóa) thuộc nó? ---- có --> 409
   xóa sản phẩm ------ còn tồn kho > 0 ở kho nào đó?      ---- có --> 409
                 ------ đang nằm trong đơn nhập ĐANG CHỜ? ---- có --> 409
   xóa kho      ------ còn tồn kho > 0?                    ---- có --> 409
                 ------ có đơn nhập ĐANG CHỜ trỏ vào?      ---- có --> 409
   xóa nhà cung cấp -- có đơn nhập ĐANG CHỜ trỏ vào?      ---- có --> 409
                                                            không --> 204, đánh dấu đã xóa
```

Lý do: xóa sản phẩm đang có 100 cái trong kho thì 100 cái đó "bay" khỏi báo cáo nhưng vẫn nằm trên kệ. Xóa kho đang có đơn chờ về thì hàng về không biết nhập vào đâu.

---

## 4. Hai điểm "khó" tôi muốn nhấn khi demo

### 4.1 Hoặc tất cả, hoặc không gì cả

Nhận hàng chạm vào ba bảng: tồn kho, nhật ký kho, đơn nhập hàng. Nếu không gói lại, có thể xảy ra:

```text
   tồn kho đã +100
   nhật ký đã ghi +100
   server lỗi ở đây  <-- đơn vẫn ĐANG CHỜ
   -> người dùng thấy đơn chưa nhận, bấm nhận lại
   -> tồn kho +100 lần nữa, kho ảo 200 trong khi hàng thật chỉ 100
```

Nên cả ba bước nằm trong **một gói**. Lỗi ở bước nào thì hủy sạch, tồn kho về như cũ, đơn vẫn chờ, bấm lại an toàn.

### 4.2 Hai người cùng nhận hàng cho cùng một sản phẩm

Tình huống: tồn kho đang 50. Hai đơn nhập khác nhau, cùng kho, cùng sản phẩm, mỗi đơn 100 cái. Hai nhân viên bấm nhận cùng lúc.

**Nếu không khóa dòng:**

```text
   thời gian   request A                request B               tồn kho trong DB
   --------    ---------------------    ---------------------   ----------------
   t1          đọc tồn kho = 50                                  50
   t2                                   đọc tồn kho = 50         50
   t3          ghi 50 + 100 = 150                                150
   t4                                   ghi 50 + 100 = 150       150   <-- SAI, phải là 250
```

Mất 100 cái hàng trên sổ. Đây gọi là lost update.

**Có khóa dòng (cách project đang làm):**

```text
   thời gian   request A                      request B                   tồn kho trong DB
   --------    ---------------------------    -------------------------   ----------------
   t1          khóa + đọc tồn kho = 50                                     50
   t2                                         xin khóa... phải chờ A       50
   t3          ghi 150, commit, trả khóa                                   150
   t4                                         lấy được khóa, đọc = 150     150
   t5                                         ghi 150 + 100 = 250, commit  250   <-- ĐÚNG
```

Cụ thể tôi khóa hai thứ: dòng đơn nhập (để không ai nhận hay thêm dòng vào cùng một đơn song song) và dòng tồn kho của từng cặp (kho, sản phẩm). Khóa là khóa ở mức database (`SELECT ... FOR UPDATE`), nên kể cả chạy nhiều instance server vẫn đúng.

Trường hợp dòng tồn kho **chưa tồn tại** (sản phẩm lần đầu về kho này): hai request cùng không thấy dòng, cùng tạo mới. Ràng buộc unique trên (kho, sản phẩm) sẽ chặn cái thứ hai. Hiện tại cái thứ hai sẽ lỗi, người dùng bấm lại là xong. Đây là một chỗ tôi còn muốn làm kỹ hơn (xem phần 8).

---

## 5. Khi có lỗi, server trả gì

Mọi lỗi đi qua một chỗ xử lý tập trung, trả về cùng một khuôn:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot receive purchase order in status: RECEIVED",
  "path": "/api/purchase-orders/.../receive"
}
```

Dịch mã lỗi sang tiếng người:

| Mã | Nghĩa là | Ví dụ trong project |
|---|---|---|
| 400 | bạn gửi dữ liệu sai | số lượng <= 0, thiếu field bắt buộc, sort theo field không có, enum sai |
| 401 | bạn chưa đưa vé hoặc vé hỏng | thiếu token, token hết hạn, sai mật khẩu khi login |
| 403 | có vé nhưng không đủ quyền | `STAFF` tạo / sửa / xóa danh mục, sản phẩm, kho, nhà cung cấp |
| 404 | cái bạn trỏ tới không tồn tại | ID sản phẩm / kho / nhà cung cấp / người nhận không có trong DB |
| 409 | tồn tại nhưng trạng thái không cho phép, hoặc trùng | nhận đơn đã nhận, trùng mã sản phẩm, xóa danh mục còn sản phẩm |
| 500 | lỗi tôi chưa lường được | có ghi log để debug |

Ý tưởng: service chỉ cần ném đúng loại lỗi, tầng web lo chuyển thành HTTP. Controller không có try/catch.

---

## 6. Kịch bản demo gợi ý (theo Postman collection)

Chạy server với dữ liệu mẫu (mỗi lần khởi động tạo lại bảng và nạp lại mẫu, ID cố định nên Postman dùng lại được).

1. **Đăng nhập**: gọi login với `admin`, lấy vé. Gọi một API bất kỳ không kèm vé, cho mentor thấy 401. Login thêm bằng `staff`, dùng vé đó tạo danh mục, cho thấy 403.
2. **Danh mục + sản phẩm**: tạo danh mục, tạo sản phẩm thuộc danh mục đó. Thử tạo sản phẩm trùng mã -> 409. Thử tạo với danh mục không tồn tại -> 404.
3. **Tạo đơn nhập hàng** kèm một dòng hàng. Thử thêm dòng với số lượng 0 -> 400. Thêm dòng hợp lệ -> 200.
4. **Xem tồn kho trước khi nhận**: chưa có dòng nào (hoặc số cũ).
5. **Nhận hàng** -> 200, trạng thái đổi sang đã nhận, có người nhận và thời điểm.
6. **Xem tồn kho sau khi nhận**: số đã tăng đúng. **Xem nhật ký kho**: có dòng IN, đúng số, đúng người.
7. **Nhận lại lần 2** -> 409. **Thêm dòng vào đơn đã nhận** -> 409.
8. **Xóa sản phẩm vừa nhận** -> 409 vì còn tồn kho. Xóa danh mục đang có sản phẩm -> 409.
9. (Tùy thời gian) Mở Swagger UI cho mentor xem toàn bộ endpoint.

Câu chốt sau mỗi bước, để nhớ nói:

- Sau bước 1: "mọi thứ sau đây đều cần vé, vé là JWT có hạn".
- Sau bước 5: "ba bảng được ghi trong một gói, lỗi giữa chừng thì hủy hết".
- Sau bước 6: "tồn kho không có API ghi, chỉ thay đổi qua nghiệp vụ, nhật ký là bằng chứng".
- Sau bước 7: "trạng thái quyết định được làm gì, không phải client tự ý".

---

## 7. Câu hỏi mentor có thể hỏi và tôi sẽ trả lời thế nào

**"Tại sao khóa bi quan (pessimistic) mà không dùng khóa lạc quan (optimistic / version)?"**
Vì nghiệp vụ nhận hàng là ghi, và xung đột là thật (nhiều người cùng nhập một sản phẩm phổ biến). Khóa lạc quan sẽ bắt một bên fail rồi retry, trải nghiệm tệ hơn với người dùng nhập liệu. Lượng request không lớn nên chi phí chờ khóa chấp nhận được. Nếu sau này đọc nhiều ghi ít thì cân nhắc lại.

**"Hai request cùng tạo dòng tồn kho mới thì sao?"**
Unique constraint chặn cái thứ hai, hiện tại trả 500 (chưa map lỗi đẹp). Hướng xử lý: bắt lỗi duplicate rồi đọc lại dòng với khóa, hoặc tạo trước dòng tồn kho = 0 khi tạo đơn. Chưa làm.

**"Tại sao nhật ký kho có loại OUT mà không có nghiệp vụ xuất?"**
Enum đã khai báo sẵn cho tương lai (xuất kho, điều chỉnh kiểm kê). Hiện chỉ có nhập nên chỉ sinh IN. Tôi để lại để không phải đổi schema sau.

**"Sao người nhận lại gửi trong body thay vì lấy từ vé?"**
Đúng là nên lấy từ vé, đó là việc tiếp theo trong checklist. Lúc làm nhận hàng thì chưa có đăng nhập, nên tạm gửi ID. Hiện ID vẫn được tra lại trong DB nên không nhận bừa.

**"Xóa mềm thì sao biết dữ liệu nào còn sống?"**
Mọi truy vấn lấy danh sách hoặc lấy để gán quan hệ đều lọc `isDeleted = false`. Danh mục đã xóa mềm không gán được cho sản phẩm mới. Dữ liệu lịch sử (đơn cũ, nhật ký cũ) vẫn join được tới bản ghi đã xóa.

**"Test đâu?"**
Hiện test chủ yếu qua Postman collection (có assert từng case). Unit test cho service nhận hàng và integration test cho tình huống hai request song song là hai việc tôi xếp ưu tiên tiếp theo. Tôi biết đây là phần yếu nhất.

**"Hủy đơn nhập (cancel) được không?"**
Chưa. Chỉ có hai trạng thái. Thêm CANCELLED là mở rộng tự nhiên, cũng chỉ cho phép từ ĐANG CHỜ.

---

## 8. Chưa làm, nói thẳng

- Chưa có unit test / integration test. Postman là lớp test duy nhất.
- 401 (thiếu vé) và 403 (không đủ quyền) hiện trả body rỗng, chưa cùng khuôn `ErrorResponse` với các lỗi khác.
- Người nhận hàng gửi qua body, chưa lấy từ vé.
- Hai request cùng tạo dòng tồn kho mới lần đầu: cái thứ hai trả 500 thay vì tự xử lý.
- Cấu hình đang xóa và tạo lại bảng mỗi lần khởi động (tiện demo, không dùng được thật). Chưa có Flyway.
- Lấy danh sách đơn nhập có N+1 query (nhà cung cấp, kho, dòng hàng), chưa tối ưu.
- Chưa có xuất kho, điều chỉnh kiểm kê, hủy đơn.
- Phân quyền mới có 2 role và chưa có API đổi role: muốn thêm ADMIN phải sửa thẳng DB.

---

## 9. Tóm lại một câu

Đây là backend quản lý nhập kho: tạo đơn, nhận hàng, tồn kho tự cộng, nhật ký tự ghi, tất cả trong một gói và có khóa dòng để hai người nhận cùng lúc không làm sai số. Phần CRUD xung quanh (danh mục, sản phẩm, kho, nhà cung cấp) có xóa mềm và chặn xóa khi còn dùng. Mọi API cần vé đăng nhập.
