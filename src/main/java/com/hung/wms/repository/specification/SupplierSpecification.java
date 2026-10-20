package com.hung.wms.repository.specification;

import com.hung.wms.model.request.supplier.SupplierSearchRequest;
import com.hung.wms.repository.entity.SupplierEntity;
import org.springframework.data.jpa.domain.Specification;

public class SupplierSpecification {
    private SupplierSpecification() {
    }

    public static Specification<SupplierEntity> search(SupplierSearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                nameContains(request.getName()),
                phoneContains(request.getPhone()),
                emailContains(request.getEmail()),
                addressContains(request.getAddress())
        );
    }

    public static Specification<SupplierEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<SupplierEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("name"), LikePattern.contains(name), LikePattern.ESCAPE);
    }

    public static Specification<SupplierEntity> phoneContains(String phone) {
        if (phone == null || phone.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("phone"), LikePattern.contains(phone), LikePattern.ESCAPE);
    }

    public static Specification<SupplierEntity> emailContains(String email) {
        if (email == null || email.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("email"), LikePattern.contains(email), LikePattern.ESCAPE);
    }

    public static Specification<SupplierEntity> addressContains(String address) {
        if (address == null || address.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("address"), LikePattern.contains(address), LikePattern.ESCAPE);
    }
}
