package com.hung.wms.dto.request.supplier;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierSearchRequest {
    private String name, phone, email, address;
}
