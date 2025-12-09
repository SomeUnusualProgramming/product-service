# Product Service API Contract

## Overview

This document describes the stable API contract for the Product Service. The API is designed to ensure backward compatibility and prevent breaking changes through proper DTO usage and exception handling.

## Architecture Principles

1. **DTO Separation**: All API endpoints work with DTOs (Request/Response), not database entities
2. **Exception Standardization**: All errors follow a unified error response format
3. **HTTP Status Codes**: Proper HTTP status codes for different scenarios
4. **Validation**: Input validation occurs at the DTO layer using Jakarta Bean Validation
5. **Logging**: All errors are logged with appropriate severity levels

## API Endpoints

### Base Path
```
/api/products
```

### 1. Get All Products
```
GET /api/products
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Product A",
    "description": "Description A",
    "category": "Category A",
    "price": 99.99,
    "stockQuantity": 100,
    "eventType": "CREATED",
    "eventTime": "2025-01-01T10:00:00",
    "originalProductId": null
  }
]
```

### 2. Get Product by ID
```
GET /api/products/{id}
```

**Response:** `200 OK` (same as single product from list)

**Error:** `404 Not Found`
```json
{
  "errorId": "550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "error": "PRODUCT_NOT_FOUND",
  "message": "Product not found with id: 123",
  "timestamp": "2025-01-01T10:00:00",
  "path": "/api/products/123",
  "fieldErrors": null,
  "traceId": null
}
```

### 3. Get Product History
```
GET /api/products/{id}/history
```

Returns all historical versions of a product including CREATED, UPDATED, DELETED events.

**Response:** `200 OK`
```json
[
  {
    "id": 10,
    "name": "Product A",
    "description": "Updated description",
    "category": "Category A",
    "price": 99.99,
    "stockQuantity": 100,
    "eventType": "UPDATED",
    "eventTime": "2025-01-01T11:00:00",
    "originalProductId": 1
  },
  {
    "id": 1,
    "name": "Product A",
    "description": "Description A",
    "category": "Category A",
    "price": 99.99,
    "stockQuantity": 100,
    "eventType": "CREATED",
    "eventTime": "2025-01-01T10:00:00",
    "originalProductId": null
  }
]
```

### 4. Create Product
```
POST /api/products
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Product A",
  "description": "Description A",
  "category": "Category A",
  "price": 99.99,
  "stockQuantity": 100
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Product A",
  "description": "Description A",
  "category": "Category A",
  "price": 99.99,
  "stockQuantity": 100,
  "eventType": "CREATED",
  "eventTime": "2025-01-01T10:00:00",
  "originalProductId": null
}
```

**Error:** `400 Bad Request` (validation failed)
```json
{
  "errorId": "550e8400-e29b-41d4-a716-446655440000",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2025-01-01T10:00:00",
  "path": "/api/products",
  "fieldErrors": {
    "name": "Product name is required",
    "price": "Price must be greater than 0"
  },
  "traceId": null
}
```

### 5. Update Product
```
PUT /api/products/{id}
Content-Type: application/json
```

**Request Body:** (same as create)
```json
{
  "name": "Updated Product A",
  "description": "Updated Description",
  "category": "Updated Category",
  "price": 199.99,
  "stockQuantity": 50
}
```

**Response:** `200 OK` (same as product response)

**Errors:**
- `404 Not Found`: Product does not exist
- `400 Bad Request`: Validation failed

### 6. Delete Product
```
DELETE /api/products/{id}
```

**Response:** `204 No Content` (empty body)

**Error:** `404 Not Found` (product does not exist)

---

## DTO Specifications

### ProductRequestDTO
Used for `POST` and `PUT` requests.

| Field | Type | Constraints | Required |
|-------|------|-----------|----------|
| name | String | 1-255 chars | Yes |
| description | String | 1-2000 chars | Yes |
| category | String | 1-100 chars | Yes |
| price | Double | > 0, max 2 decimals | Yes |
| stockQuantity | Integer | >= 0 | Yes |

### ProductResponseDTO
Returned in all successful responses.

| Field | Type | Notes |
|-------|------|-------|
| id | Long | Product identifier |
| name | String | Product name |
| description | String | Product description |
| category | String | Product category |
| price | Double | Product price |
| stockQuantity | Integer | Available stock |
| eventType | String | CREATED, UPDATED, DELETED, LOW_STOCK |
| eventTime | LocalDateTime | Event timestamp |
| originalProductId | Long | Parent product ID (null for current versions) |

### ErrorResponse
Returned on all errors.

| Field | Type | Notes |
|-------|------|-------|
| errorId | String | UUID for error tracking |
| status | Integer | HTTP status code |
| error | String | Error code (see Error Codes below) |
| message | String | Human-readable error message |
| timestamp | LocalDateTime | Error occurrence time |
| path | String | Request URI |
| fieldErrors | Map | Field-specific validation errors (nullable) |
| traceId | String | Request trace ID (for correlation) |

---

## Error Codes

| Error Code | HTTP Status | Description |
|-----------|------------|-------------|
| PRODUCT_NOT_FOUND | 404 | Requested product does not exist |
| RESOURCE_NOT_FOUND | 404 | Generic resource not found |
| VALIDATION_ERROR | 400 | Input validation failed |
| INVALID_ARGUMENT | 400 | Illegal argument provided |
| CONFLICT | 409 | Business logic conflict |
| UNAUTHORIZED | 401 | Authentication/authorization failed |
| METHOD_NOT_ALLOWED | 405 | HTTP method not allowed for endpoint |
| ENDPOINT_NOT_FOUND | 404 | Endpoint does not exist |
| INTERNAL_SERVER_ERROR | 500 | Unexpected server error |

---

## Exception Handling

All exceptions are caught by `GlobalExceptionHandler` and converted to standardized `ErrorResponse` objects.

### Exception Hierarchy

```
Exception
├── ProductNotFoundException
│   └── extends ResourceNotFoundException
├── ValidationException
├── BusinessException
├── ConflictException
└── UnauthorizedException
```

### Exception Details

**ProductNotFoundException**
- Status: 404
- Error Code: PRODUCT_NOT_FOUND
- Example: Product with specified ID not found

**ResourceNotFoundException**
- Status: 404
- Error Code: RESOURCE_NOT_FOUND
- Generic resource not found exception

**ValidationException**
- Status: 400
- Error Code: VALIDATION_ERROR
- Contains field-level error details

**BusinessException**
- Status: 400
- Error Code: Custom (includes errorCode property)
- Business logic violations

**ConflictException**
- Status: 409
- Error Code: Custom (includes errorCode property)
- Data conflicts (e.g., duplicate entries)

**UnauthorizedException**
- Status: 401
- Error Code: UNAUTHORIZED
- Authentication or authorization failures

---

## Validation Rules

### Product Name
- Required (not blank)
- Length: 1-255 characters
- Error: "Product name is required" or "Product name must be between 1 and 255 characters"

### Product Description
- Required (not blank)
- Length: 1-2000 characters
- Error: "Product description is required" or "Product description must be between 1 and 2000 characters"

### Product Category
- Required (not blank)
- Length: 1-100 characters
- Error: "Product category is required" or "Product category must be between 1 and 100 characters"

### Product Price
- Required (not null)
- Value: > 0 (must be positive)
- Decimals: max 2 fractional digits (10 integer + 2 decimal)
- Error: "Product price is required", "Price must be greater than 0", or "Price must have at most 10 integer digits and 2 fractional digits"

### Stock Quantity
- Required (not null)
- Value: >= 0 (non-negative)
- Error: "Stock quantity is required" or "Stock quantity cannot be negative"

---

## API Stability

### Backward Compatibility Guarantees

1. **No Field Removals**: Once a field is added to a DTO, it will never be removed
2. **No Type Changes**: Field types will not change (e.g., String to Integer)
3. **New Fields Optional**: New fields will always be optional with sensible defaults
4. **Error Code Stability**: Error codes will remain stable; new codes may be added but existing codes won't change
5. **Endpoint Stability**: Existing endpoints will not be removed; new endpoints may be added

### Breaking Changes (Prevented)

- Direct exposure of database entities (prevented by DTO layer)
- Unplanned schema changes (prevented by contract-based testing)
- Inconsistent error responses (enforced by GlobalExceptionHandler)
- Unstable validation rules (defined in constants and DTOs)

---

## Multitenancy

The Product Service implements tenant isolation:
- All products are scoped to a tenant ID
- Tenant ID is extracted from request context
- Products from different tenants are never mixed in responses
- All queries automatically filter by tenant ID

---

## Audit Trail

The Product Service maintains audit history for all products:

**Event Types:**
- `CREATED`: Product was created
- `UPDATED`: Product was updated
- `DELETED`: Product was deleted
- `LOW_STOCK`: Stock fell below threshold (future use)

History is accessible via `GET /api/products/{id}/history`

---

## Rate Limiting & Quotas

Currently not implemented. Reserved for future use.

---

## Versioning Strategy

This API does not use URL versioning (e.g., `/v1/products`). Instead, it relies on:
1. Backward-compatible DTOs
2. Optional new fields
3. Graceful error handling

If a major breaking change becomes necessary, a new versioned endpoint will be introduced alongside the current one, with a deprecation notice.

---

## Client Best Practices

1. **Always validate status codes**: Don't assume success
2. **Handle field errors**: Parse `fieldErrors` map in VALIDATION_ERROR responses
3. **Use errorId for support**: Include errorId when reporting issues
4. **Respect HTTP semantics**: 200/201 = success, 4xx = client error, 5xx = server error
5. **Don't parse error messages**: Use error codes for logic; error messages are for UI display

---

## API Stability Checklist

- ✅ DTOs separate from entities
- ✅ Standardized error responses
- ✅ Comprehensive validation
- ✅ Exception hierarchy
- ✅ HTTP status code compliance
- ✅ Audit trail tracking
- ✅ Tenant isolation
- ✅ Backward compatibility guarantees
- ✅ Field immutability in responses
- ✅ Clear error codes

---

## Last Updated
2025-09-12

## Maintained By
ASW
