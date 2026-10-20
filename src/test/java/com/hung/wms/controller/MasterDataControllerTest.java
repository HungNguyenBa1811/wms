package com.hung.wms.controller;

import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.category.CategoryRequest;
import com.hung.wms.dto.request.warehouse.WarehouseRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.supplier.SupplierResponse;
import com.hung.wms.service.CategoryService;
import com.hung.wms.service.SupplierService;
import com.hung.wms.service.WarehouseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Category / Warehouse / Supplier share the same pattern as Product, so one slice covers the three controllers
@WebMvcTest({CategoryController.class, WarehouseController.class, SupplierController.class})
@WithMockUser(roles = "ADMIN")
@DisplayName("Category / Warehouse / Supplier controller (@WebMvcTest)")
class MasterDataControllerTest extends ControllerSliceTestSupport {
    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private SupplierService supplierService;

    // ---------- Category ----------

    @Test
    @DisplayName("POST /api/categories trùng name -> 409")
    void createDuplicateCategory() throws Exception {
        // given
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenThrow(new ResourceDuplicateException("Category name already exists"));

        // when / then
        mockMvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Electronics\"}"))
                .andExpect(errorResponse(409, "Conflict", "Category name already exists", "/api/categories"));
    }

    @Test
    @DisplayName("POST /api/categories thiếu name -> 400")
    void createCategoryWithoutName() throws Exception {
        mockMvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/categories"))
                .andExpect(jsonPath("$.detail[0]").value("name: Name is required"));
        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} còn product -> 409")
    void deleteCategoryInUse() throws Exception {
        // given
        doThrow(new ResourceInUseException("Cannot delete category c-1: it still has products"))
                .when(categoryService).deleteCategoryById("c-1");

        // when / then
        mockMvc.perform(delete("/api/categories/c-1"))
                .andExpect(errorResponse(409, "Conflict", "Cannot delete category c-1: it still has products", "/api/categories/c-1"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} -> 204")
    void deleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/c-1"))
                .andExpect(status().isNoContent());
        verify(categoryService).deleteCategoryById("c-1");
    }

    // ---------- Warehouse ----------

    @Test
    @DisplayName("POST /api/warehouses thiếu field bắt buộc -> 400")
    void createWarehouseMissingFields() throws Exception {
        mockMvc.perform(post("/api/warehouses").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Main\"}"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/warehouses"))
                .andExpect(jsonPath("$.detail", containsInAnyOrder(
                        "warehouseCode: Warehouse code is required",
                        "location: Location is required")));
        verifyNoInteractions(warehouseService);
    }

    @Test
    @DisplayName("PUT /api/warehouses/{id} đổi sang mã đã có -> 409")
    void updateWarehouseDuplicateCode() throws Exception {
        // given
        when(warehouseService.updateWarehouse(eq("w-1"), any(WarehouseRequest.class)))
                .thenThrow(new ResourceDuplicateException("Warehouse code already exists"));

        // when / then
        mockMvc.perform(put("/api/warehouses/w-1").contentType(MediaType.APPLICATION_JSON).content("{\"warehouseCode\": \"WH-002\"}"))
                .andExpect(errorResponse(409, "Conflict", "Warehouse code already exists", "/api/warehouses/w-1"));
    }

    @Test
    @DisplayName("GET /api/warehouses/{id} không tồn tại -> 404")
    void getWarehouseNotFound() throws Exception {
        // given
        when(warehouseService.findWarehouseById("missing"))
                .thenThrow(new ResourceNotFoundException("Warehouse not found with id: missing"));

        // when / then
        mockMvc.perform(get("/api/warehouses/missing"))
                .andExpect(errorResponse(404, "Not Found", "Warehouse not found with id: missing", "/api/warehouses/missing"));
    }

    // ---------- Supplier ----------

    @Test
    @DisplayName("GET /api/suppliers -> 200, sort mặc định createdAt DESC")
    void listSuppliers() throws Exception {
        // given
        SupplierResponse supplier = new SupplierResponse();
        supplier.setName("ACME");
        when(supplierService.findAllSuppliers(any(), any()))
                .thenReturn(new PageResponse<>(new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1)));

        // when / then
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("ACME"));
        verify(supplierService).findAllSuppliers(any(), eq(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Test
    @DisplayName("POST /api/suppliers email / phone sai -> 400")
    void createSupplierInvalidContact() throws Exception {
        mockMvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ACME\", \"email\": \"not-an-email\", \"phone\": \"12\"}"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/suppliers"))
                .andExpect(jsonPath("$.detail", containsInAnyOrder(
                        "email: Email is invalid",
                        "phone: Phone must be 8-15 digits, optionally starting with +")));
        verifyNoInteractions(supplierService);
    }

    @Test
    @DisplayName("DELETE /api/suppliers/{id} có PO PENDING -> 409")
    void deleteSupplierInUse() throws Exception {
        // given
        doThrow(new ResourceInUseException("Cannot delete supplier s-1: it has a pending purchase order"))
                .when(supplierService).deleteSupplierById("s-1");

        // when / then
        mockMvc.perform(delete("/api/suppliers/s-1"))
                .andExpect(errorResponse(409, "Conflict", "Cannot delete supplier s-1: it has a pending purchase order",
                        "/api/suppliers/s-1"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("STAFF đọc dữ liệu nền -> 200, ghi -> 403")
    void staffReadsButCannotWrite() throws Exception {
        // given
        when(categoryService.findAllCategories(any(), any()))
                .thenReturn(new PageResponse<>(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0)));

        // when / then
        mockMvc.perform(get("/api/categories")).andExpect(status().isOk());
        mockMvc.perform(put("/api/warehouses/w-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
        mockMvc.perform(delete("/api/suppliers/s-1")).andExpect(status().isForbidden());
        verifyNoInteractions(warehouseService, supplierService);
    }
}
