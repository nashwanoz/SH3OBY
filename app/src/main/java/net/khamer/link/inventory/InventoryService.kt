package net.khamer.link.inventory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.khamer.link.data.dao.ProductDao
import net.khamer.link.data.dao.StockDao
import net.khamer.link.data.entities.InvoiceLine
import net.khamer.link.data.entities.ProductUnit
import net.khamer.link.data.entities.Stock
import net.khamer.link.data.entities.StockTransaction
import java.util.*

class InventoryService(
    private val productDao: ProductDao,
    private val stockDao: StockDao
) {
    suspend fun applyInvoiceLineToStock(line: InvoiceLine) = withContext(Dispatchers.IO) {
        val unit: ProductUnit = productDao.unitsForProduct(line.productId).firstOrNull { it.id == line.productUnitId }
            ?: throw Exception("وحدة المنتج غير موجودة")
        val stock = stockDao.getByProductId(line.productId) ?: Stock(line.productId, 0L)
        val deltaBase = line.qty * unit.multiplierToBase
        if (stock.quantityInBase < deltaBase) throw Exception("الكمية غير كافية في المخزون")
        val newStock = stock.copy(quantityInBase = stock.quantityInBase - deltaBase)
        stockDao.insertTransaction(StockTransaction(
            id = UUID.randomUUID().toString(),
            productId = line.productId,
            deltaInBase = -deltaBase,
            reason = "sale",
            refId = line.invoiceId,
            date = System.currentTimeMillis(),
            userId = line.userId
        ))
        stockDao.insertStock(newStock)
    }
}
