package com.hung.wms.api;

import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.InvalidStateException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.model.response.user.UserResponse;
import com.hung.wms.service.PurchaseOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The whole PO flow only needs authenticated(), so STAFF is enough everywhere here
@WebMvcTest(PurchaseOrderAPI.class)
@WithMockUser(username = "staff", roles = "STAFF")
@DisplayName("PurchaseOrderAPI (@WebMvcTest)")
class PurchaseOrderAPITest extends ApiSliceTestSupport {
    private static final String PO_ID = "po-1";
    private static final String VALID_BODY = """
            {"supplierId": "supplier-1", "warehouseId": "warehouse-1",
             "items": [{"productId": "product-1", "quantity": 5, "unitCost": 10.00}]}
            """;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @Test
    @DisplayName("POST /api/purchase-orders hợp lệ -> 200, body là PurchaseOrderResponse")
    void createPurchaseOrder() throws Exception {
        // given
        when(purchaseOrderService.createPurchaseOrder(any(PurchaseOrderRequest.class))).thenReturn(response("PENDING"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PO_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
        ArgumentCaptor<PurchaseOrderRequest> request = ArgumentCaptor.forClass(PurchaseOrderRequest.class);
        verify(purchaseOrderService).createPurchaseOrder(request.capture());
        assertThat(request.getValue().getItems()).hasSize(1);
        assertThat(request.getValue().getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("POST /api/purchase-orders quantity <= 0, unitCost < 0 -> 400 detail theo từng item")
    void createWithInvalidItems() throws Exception {
        // given
        String body = """
                {"supplierId": "supplier-1", "warehouseId": "warehouse-1",
                 "items": [{"productId": "product-1", "quantity": 0, "unitCost": -1}]}
                """;

        // when / then
        mockMvc.perform(post("/api/purchase-orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/purchase-orders"))
                .andExpect(jsonPath("$.detail", containsInAnyOrder(
                        "items[0].quantity: Quantity must be greater than 0",
                        "items[0].unitCost: Unit cost must not be negative")));
        verifyNoInteractions(purchaseOrderService);
    }

    @Test
    @DisplayName("POST /api/purchase-orders không có item -> 400")
    void createWithoutItems() throws Exception {
        // given
        String body = """
                {"supplierId": "supplier-1", "warehouseId": "warehouse-1", "items": []}
                """;

        // when / then
        mockMvc.perform(post("/api/purchase-orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/purchase-orders"))
                .andExpect(jsonPath("$.detail[0]").value("items: Purchase order must have at least one item"));
        verifyNoInteractions(purchaseOrderService);
    }

    @Test
    @DisplayName("POST /api/purchase-orders supplier không tồn tại -> 404")
    void createWithMissingSupplier() throws Exception {
        // given
        when(purchaseOrderService.createPurchaseOrder(any(PurchaseOrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Supplier not found with id: supplier-1"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(errorResponse(404, "Not Found", "Supplier not found with id: supplier-1", "/api/purchase-orders"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders trùng product trong request -> 400")
    void createWithDuplicateProduct() throws Exception {
        // given
        when(purchaseOrderService.createPurchaseOrder(any(PurchaseOrderRequest.class)))
                .thenThrow(new BadRequestException("Product already exists in this purchase order"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(errorResponse(400, "Bad Request", "Product already exists in this purchase order", "/api/purchase-orders"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/items vào PO RECEIVED -> 409")
    void addItemToReceivedPurchaseOrder() throws Exception {
        // given
        when(purchaseOrderService.addPurchaseOrderItem(eq(PO_ID), any(PurchaseOrderItemRequest.class)))
                .thenThrow(new InvalidStateException("Cannot add item to purchase order in status: RECEIVED"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders/{id}/items", PO_ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": \"product-2\", \"quantity\": 1, \"unitCost\": 0}"))
                .andExpect(errorResponse(409, "Conflict", "Cannot add item to purchase order in status: RECEIVED",
                        "/api/purchase-orders/po-1/items"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/receive -> 200, service nhận Principal của user đăng nhập")
    void receivePurchaseOrder() throws Exception {
        // given
        when(purchaseOrderService.receivePurchaseOrder(eq(PO_ID), any(Principal.class))).thenReturn(response("RECEIVED"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders/{id}/receive", PO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.receivedBy.username").value("staff"));
        ArgumentCaptor<Principal> principal = ArgumentCaptor.forClass(Principal.class);
        verify(purchaseOrderService).receivePurchaseOrder(eq(PO_ID), principal.capture());
        assertThat(principal.getValue().getName()).isEqualTo("staff");
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/receive lần hai -> 409")
    void receiveTwice() throws Exception {
        // given
        when(purchaseOrderService.receivePurchaseOrder(eq(PO_ID), any(Principal.class)))
                .thenThrow(new InvalidStateException("Cannot receive purchase order in status: RECEIVED"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders/{id}/receive", PO_ID))
                .andExpect(errorResponse(409, "Conflict", "Cannot receive purchase order in status: RECEIVED",
                        "/api/purchase-orders/po-1/receive"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/receive PO rỗng -> 400")
    void receiveEmpty() throws Exception {
        // given
        when(purchaseOrderService.receivePurchaseOrder(eq(PO_ID), any(Principal.class)))
                .thenThrow(new BadRequestException("Cannot receive purchase order without items"));

        // when / then
        mockMvc.perform(post("/api/purchase-orders/{id}/receive", PO_ID))
                .andExpect(errorResponse(400, "Bad Request", "Cannot receive purchase order without items",
                        "/api/purchase-orders/po-1/receive"));
    }

    @Test
    @DisplayName("GET /api/purchase-orders/{id} không tồn tại -> 404")
    void getNotFound() throws Exception {
        // given
        when(purchaseOrderService.findPurchaseOrderById("missing"))
                .thenThrow(new ResourceNotFoundException("Purchase order not found with id: missing"));

        // when / then
        mockMvc.perform(get("/api/purchase-orders/missing"))
                .andExpect(errorResponse(404, "Not Found", "Purchase order not found with id: missing",
                        "/api/purchase-orders/missing"));
    }

    @Test
    @DisplayName("GET /api/purchase-orders?status=FOO -> 400 status: invalid value 'FOO'")
    void listWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/purchase-orders").param("status", "FOO"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/purchase-orders"))
                .andExpect(jsonPath("$.detail[0]").value("status: invalid value 'FOO'"));
        verifyNoInteractions(purchaseOrderService);
    }

    @Test
    @DisplayName("POST /api/purchase-orders body rỗng -> 400 Request body is missing or malformed")
    void createWithoutBody() throws Exception {
        mockMvc.perform(post("/api/purchase-orders").contentType(MediaType.APPLICATION_JSON))
                .andExpect(errorResponse(400, "Bad Request", "Request body is missing or malformed", "/api/purchase-orders"));
    }

    private static PurchaseOrderResponse response(String status) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(PO_ID);
        response.setStatus(status);
        if ("RECEIVED".equals(status)) {
            UserResponse user = new UserResponse();
            user.setUsername("staff");
            response.setReceivedBy(user);
        }
        return response;
    }
}
