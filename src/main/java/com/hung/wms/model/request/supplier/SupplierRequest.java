package com.hung.wms.model.request.supplier;

import com.hung.wms.validation.OnCreate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {
    @NotBlank(groups = OnCreate.class, message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone must be 8-15 digits, optionally starting with +")
    private String phone;

    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;
}
