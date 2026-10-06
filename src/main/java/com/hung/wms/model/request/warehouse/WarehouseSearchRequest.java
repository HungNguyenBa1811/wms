package com.hung.wms.model.request.warehouse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseSearchRequest {
    private String warehouseCode, name, location;
}
