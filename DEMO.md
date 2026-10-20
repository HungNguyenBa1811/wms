# 14/10/2026
- Mentor feedback:
```
product cần có thời gian nhập, thời gian hết hạn
các entity khác liên quan cần có người nhập, thời gian nhập
cần có base entity ở giữa chứa id và thông tin chung
monolith -> hướng microservice
chia ra thành các features

API -> Controller
Converter -> Mapper/Mapping
Exception -> tạo Generic Exception, bắn đi gồm httpstatus, message, data if there
Model -> DTO
Entity -> Move ra ngoài
Specification -> tạo 1 cái chung chứ nhiều cái sheesh
repository -> tương tự, tạo 1 cái để gen ra crud

thường thì:
controller - handle input validation
service - check business logic

triển khai theo hướng microservice: từng service sẽ có từng cụm, mỗi service có thể có 3-4 API
có thể tách auth ra thành service riêng
có thể tạo API gateway```