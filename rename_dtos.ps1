$serverDir = "D:\ERP\erp-parent\erp-server\src\main\java\com\fy\erp"
$files = Get-ChildItem -Path $serverDir -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName

# DTO renames (old -> new class name, package stays same)
$map = @{
    "LoginRequest;" = "LoginRequestDTO;"
    "LoginRequest " = "LoginRequestDTO "
    "LoginResponse;" = "LoginResponseDTO;"
    "LoginResponse " = "LoginResponseDTO "
    "LoginResponse(" = "LoginResponseDTO("
    "SmsLoginRequest;" = "SmsLoginRequestDTO;"
    "SmsLoginRequest " = "SmsLoginRequestDTO "
    "UserCreateRequest;" = "UserCreateRequestDTO;"
    "UserCreateRequest " = "UserCreateRequestDTO "
    "UserUpdateRequest;" = "UserUpdateRequestDTO;"
    "UserUpdateRequest " = "UserUpdateRequestDTO "
    "SalesOrderCreateRequest;" = "SalesOrderCreateRequestDTO;"
    "SalesOrderCreateRequest " = "SalesOrderCreateRequestDTO "
    "SalesOrderItemRequest;" = "SalesOrderItemRequestDTO;"
    "SalesOrderItemRequest " = "SalesOrderItemRequestDTO "
    "PurchaseOrderCreateRequest;" = "PurchaseOrderCreateRequestDTO;"
    "PurchaseOrderCreateRequest " = "PurchaseOrderCreateRequestDTO "
    "PurchaseOrderItemRequest;" = "PurchaseOrderItemRequestDTO;"
    "PurchaseOrderItemRequest " = "PurchaseOrderItemRequestDTO "
    "PurchaseRequestCreateRequest;" = "PurchaseRequestCreateRequestDTO;"
    "PurchaseRequestCreateRequest " = "PurchaseRequestCreateRequestDTO "
    "PurchaseRequestItemRequest;" = "PurchaseRequestItemRequestDTO;"
    "PurchaseRequestItemRequest " = "PurchaseRequestItemRequestDTO "
    "PaymentCreateRequest;" = "PaymentCreateRequestDTO;"
    "PaymentCreateRequest " = "PaymentCreateRequestDTO "
    "ReceiptCreateRequest;" = "ReceiptCreateRequestDTO;"
    "ReceiptCreateRequest " = "ReceiptCreateRequestDTO "
    "StockCheckCreateRequest;" = "StockCheckCreateRequestDTO;"
    "StockCheckCreateRequest " = "StockCheckCreateRequestDTO "
    "StockCheckItemRequest;" = "StockCheckItemRequestDTO;"
    "StockCheckItemRequest " = "StockCheckItemRequestDTO "
    "StockTransferCreateRequest;" = "StockTransferCreateRequestDTO;"
    "StockTransferCreateRequest " = "StockTransferCreateRequestDTO "
    "StockTransferItemRequest;" = "StockTransferItemRequestDTO;"
    "StockTransferItemRequest " = "StockTransferItemRequestDTO "
    # Report DTOs
    "DashboardSummary;" = "DashboardSummaryDTO;"
    "DashboardSummary " = "DashboardSummaryDTO "
    "DashboardSummary>" = "DashboardSummaryDTO>"
    "FinanceSummary;" = "FinanceSummaryDTO;"
    "FinanceSummary " = "FinanceSummaryDTO "
    "FinanceSummary>" = "FinanceSummaryDTO>"
    "LowStockItem;" = "LowStockItemDTO;"
    "LowStockItem " = "LowStockItemDTO "
    "LowStockItem>" = "LowStockItemDTO>"
    "SalesAmountByDay;" = "SalesAmountByDayDTO;"
    "SalesAmountByDay " = "SalesAmountByDayDTO "
    "SalesAmountByDay>" = "SalesAmountByDayDTO>"
    "SalesByCustomer;" = "SalesByCustomerDTO;"
    "SalesByCustomer " = "SalesByCustomerDTO "
    "SalesByCustomer>" = "SalesByCustomerDTO>"
    "SalesByProduct;" = "SalesByProductDTO;"
    "SalesByProduct " = "SalesByProductDTO "
    "SalesByProduct>" = "SalesByProductDTO>"
    "SalesFunnelItem;" = "SalesFunnelItemDTO;"
    "SalesFunnelItem " = "SalesFunnelItemDTO "
    "SalesFunnelItem>" = "SalesFunnelItemDTO>"
    "SalesRankItem;" = "SalesRankItemDTO;"
    "SalesRankItem " = "SalesRankItemDTO "
    "SalesRankItem>" = "SalesRankItemDTO>"
    # Moved classes: security + exception + util
    "com.fy.erp.security.UserContext" = "com.fy.erp.security.UserContext"
    "com.fy.erp.security.UserPrincipal" = "com.fy.erp.security.UserPrincipal"
    "com.fy.erp.exception.BizException" = "com.fy.erp.exception.BizException"
    "com.fy.erp.util.OrderNoUtil" = "com.fy.erp.utils.OrderNoUtil"
}

$count = 0
foreach ($f in $files) {
    $content = [IO.File]::ReadAllText($f, [Text.Encoding]::UTF8)
    $original = $content
    foreach ($k in $map.Keys) {
        $content = $content.Replace($k, $map[$k])
    }
    if ($content -ne $original) {
        [IO.File]::WriteAllText($f, $content, [Text.Encoding]::UTF8)
        $count++
        Write-Output "Updated: $([IO.Path]::GetFileName($f))"
    }
}
Write-Output "`nTotal updated: $count files"
