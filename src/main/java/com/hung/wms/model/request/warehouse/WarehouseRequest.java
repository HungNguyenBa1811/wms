package com.hung.wms.model.request.warehouse;

import com.hung.wms.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseRequest {
    @NotBlank(groups = OnCreate.class, message = "Warehouse code is required")
    @Size(max = 255, message = "Warehouse code must be at most 255 characters")
    private String warehouseCode;

    @NotBlank(groups = OnCreate.class, message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(groups = OnCreate.class, message = "Location is required")
    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;
}
