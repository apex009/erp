import os, glob

server_dir = r"D:\ERP\erp-parent\erp-server\src\main\java\com\fy\erp"

# Map old class names to new names (with context suffixes to avoid partial matches)
replacements = [
    # DTO class renames — order matters for overlapping names
    ("PurchaseRequestItemRequest", "PurchaseRequestItemRequestDTO"),
    ("PurchaseRequestCreateRequest", "PurchaseRequestCreateRequestDTO"),
    ("PurchaseOrderItemRequest", "PurchaseOrderItemRequestDTO"),
    ("PurchaseOrderCreateRequest", "PurchaseOrderCreateRequestDTO"),
    ("SalesOrderItemRequest", "SalesOrderItemRequestDTO"),
    ("SalesOrderCreateRequest", "SalesOrderCreateRequestDTO"),
    ("StockTransferItemRequest", "StockTransferItemRequestDTO"),
    ("StockTransferCreateRequest", "StockTransferCreateRequestDTO"),
    ("StockCheckItemRequest", "StockCheckItemRequestDTO"),
    ("StockCheckCreateRequest", "StockCheckCreateRequestDTO"),
    ("PaymentCreateRequest", "PaymentCreateRequestDTO"),
    ("ReceiptCreateRequest", "ReceiptCreateRequestDTO"),
    ("UserCreateRequest", "UserCreateRequestDTO"),
    ("UserUpdateRequest", "UserUpdateRequestDTO"),
    ("LoginResponse", "LoginResponseDTO"),
    ("LoginRequest", "LoginRequestDTO"),
    ("SmsLoginRequest", "SmsLoginRequestDTO"),
    # Report DTOs
    ("DashboardSummary", "DashboardSummaryDTO"),
    ("FinanceSummary", "FinanceSummaryDTO"),
    ("LowStockItem", "LowStockItemDTO"),
    ("SalesAmountByDay", "SalesAmountByDayDTO"),
    ("SalesByCustomer", "SalesByCustomerDTO"),
    ("SalesByProduct", "SalesByProductDTO"),
    ("SalesFunnelItem", "SalesFunnelItemDTO"),
    ("SalesRankItem", "SalesRankItemDTO"),
    # Moved package: util -> utils
    ("com.fy.erp.util.OrderNoUtil", "com.fy.erp.utils.OrderNoUtil"),
]

count = 0
for root, dirs, files in os.walk(server_dir):
    # Skip dto directory itself (those will be deleted)
    if "\\dto\\" in root or "/dto/" in root:
        continue
    for fname in files:
        if not fname.endswith(".java"):
            continue
        fpath = os.path.join(root, fname)
        with open(fpath, "r", encoding="utf-8") as f:
            content = f.read()
        original = content
        for old, new in replacements:
            content = content.replace(old, new)
        if content != original:
            with open(fpath, "w", encoding="utf-8") as f:
                f.write(content)
            count += 1
            print(f"Updated: {fname}")

print(f"\nTotal updated: {count} files")
