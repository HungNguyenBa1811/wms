package com.hung.wms.controller;

import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.product.ProductRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.product.ProductResponse;
import com.hung.wms.service.ProductService;
import io.jsonwebtoken.JwtException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController (@WebMvcTest)")
class ProductControllerTest extends ControllerSliceTestSupport {
    private static final String VALID_BODY = """
            {"productCode": "P-001", "name": "Mouse", "price": 15.50, "categoryId": "category-1"}
            """;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("GET /api/products -> 200 PageResponse, sort mặc định createdAt DESC, size 20")
    void listProducts() throws Exception {
        // given
        ProductResponse product = new ProductResponse();
        product.setId("product-1");
        product.setProductCode("P-001");
        when(productService.findAllProducts(any(), any()))
                .thenReturn(new PageResponse<>(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1)));

        // when / then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productCode").value("P-001"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(productService).findAllProducts(any(), eq(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("GET /api/products?priceFrom=abc -> 400 detail chỉ ra field")
    void listWithInvalidQueryParam() throws Exception {
        mockMvc.perform(get("/api/products").param("priceFrom", "abc"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/products"))
                .andExpect(jsonPath("$.detail[0]").value("priceFrom: invalid value 'abc'"));
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("GET /api/products/{id} không tồn tại -> 404 ErrorResponse")
    void getNotFound() throws Exception {
        // given
        when(productService.findProductById("missing"))
                .thenThrow(new ResourceNotFoundException("Product not found with id: missing"));

        // when / then
        mockMvc.perform(get("/api/products/missing"))
                .andExpect(errorResponse(404, "Not Found", "Product not found with id: missing", "/api/products/missing"))
                .andExpect(jsonPath("$.detail").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products hợp lệ -> 200 (controller hiện trả 200, không phải 201)")
    void createProduct() throws Exception {
        // given
        ProductResponse created = new ProductResponse();
        created.setId("product-1");
        created.setProductCode("P-001");
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(created);

        // when / then
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("product-1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products thiếu field bắt buộc, giá <= 0 -> 400 liệt kê từng field")
    void createInvalidBody() throws Exception {
        // given
        String body = """
                {"name": "Mouse", "price": 0}
                """;

        // when / then
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/products"))
                .andExpect(jsonPath("$.detail", containsInAnyOrder(
                        "productCode: Product code is required",
                        "categoryId: Category is required",
                        "price: Price must be greater than 0")));
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products expiryDate trước importedAt, importedAt ở tương lai -> 400")
    void createInvalidDates() throws Exception {
        // given
        String body = """
                {"productCode": "P-001", "name": "Milk", "price": 1.5, "categoryId": "category-1",
                 "importedAt": "2999-01-10", "expiryDate": "2999-01-01"}
                """;

        // when / then
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/products"))
                .andExpect(jsonPath("$.detail", containsInAnyOrder(
                        "importedAt: Imported date must not be in the future",
                        "expiryDateValid: Expiry date must not be before imported date")));
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("GET /api/products?expiryBefore=2026-13-01 -> 400")
    void listWithInvalidExpiryBefore() throws Exception {
        mockMvc.perform(get("/api/products").param("expiryBefore", "2026-13-01"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/products"))
                .andExpect(jsonPath("$.detail[0]").value("expiryBefore: invalid value '2026-13-01'"));
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products body hỏng -> 400 Request body is missing or malformed")
    void createMalformedBody() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"price\": \"abc\"}"))
                .andExpect(errorResponse(400, "Bad Request", "Request body is missing or malformed", "/api/products"))
                .andExpect(jsonPath("$.detail[0]").value("price: invalid value 'abc'"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products trùng productCode -> 409 ErrorResponse")
    void createDuplicate() throws Exception {
        // given
        when(productService.createProduct(any(ProductRequest.class)))
                .thenThrow(new ResourceDuplicateException("Product code already exists"));

        // when / then
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(errorResponse(409, "Conflict", "Product code already exists", "/api/products"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/products/{id} partial update không cần field bắt buộc -> 200")
    void partialUpdate() throws Exception {
        // given
        when(productService.updateProduct(eq("product-1"), any(ProductRequest.class))).thenReturn(new ProductResponse());

        // when / then
        mockMvc.perform(put("/api/products/product-1").contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"New\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/products/{id} -> 204 không có body")
    void deleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/product-1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(productService).deleteProductById("product-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/products/{id} còn tồn kho -> 409 ErrorResponse")
    void deleteInUse() throws Exception {
        // given
        doThrow(new ResourceInUseException("Cannot delete product product-1: it still has stock in inventory"))
                .when(productService).deleteProductById("product-1");

        // when / then
        mockMvc.perform(delete("/api/products/product-1"))
                .andExpect(errorResponse(409, "Conflict",
                        "Cannot delete product product-1: it still has stock in inventory", "/api/products/product-1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/products/{id} -> 405 vẫn là ErrorResponse")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(patch("/api/products/product-1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    // ---------- security ----------

    @Test
    @DisplayName("không có token -> 401 ErrorResponse Authentication is required")
    void missingToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/products"));
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("token sai chữ ký / hết hạn -> filter bỏ qua, 401 thay vì 500")
    void invalidToken() throws Exception {
        // given
        when(jwtService.extractUsername("broken.token.value")).thenThrow(new JwtException("bad signature"));

        // when / then
        mockMvc.perform(get("/api/products").header("Authorization", "Bearer broken.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
        verifyNoInteractions(productService, userDetailsService);
    }

    @Test
    @DisplayName("token hợp lệ: role lấy từ claim, filter không tra bảng users (không gọi UserDetailsService)")
    void validTokenUsesRoleClaim() throws Exception {
        // given
        when(jwtService.extractUsername("staff.token")).thenReturn("staff");
        when(jwtService.extractRole("staff.token")).thenReturn("STAFF");

        // when / then: STAFF đọc được, ghi dữ liệu nền bị 403 theo role trong token
        mockMvc.perform(get("/api/products").header("Authorization", "Bearer staff.token"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer staff.token")
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("token cũ không có claim role -> 401, phải đăng nhập lại")
    void tokenWithoutRoleClaim() throws Exception {
        // given
        when(jwtService.extractUsername("old.token")).thenReturn("staff");
        when(jwtService.extractRole("old.token")).thenReturn(null);

        // when / then
        mockMvc.perform(get("/api/products").header("Authorization", "Bearer old.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
        verifyNoInteractions(productService, userDetailsService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("STAFF ghi dữ liệu nền -> 403 ErrorResponse")
    void staffCannotCreate() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"))
                .andExpect(jsonPath("$.path").value("/api/products"));
        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    @DisplayName("STAFF xóa product -> 403")
    void staffCannotDelete() throws Exception {
        mockMvc.perform(delete("/api/products/product-1"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(productService);
    }
}
