package net.khamer.link.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.khamer.link.data.dao.PriceDao
import net.khamer.link.data.dao.ProductDao
import net.khamer.link.data.entities.Product
import net.khamer.link.data.entities.ProductPrice
import net.khamer.link.data.entities.ProductUnit
import java.util.*

data class UnitForm(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var multiplier: String = "1",
    var price: String = "0.0"
)

class AddProductViewModel(
    private val productDao: ProductDao,
    private val priceDao: PriceDao
) : ViewModel() {
    private val _units = MutableStateFlow(mutableListOf<UnitForm>(UnitForm()))
    val units: StateFlow<MutableList<UnitForm>> = _units

    val productName = MutableStateFlow("")
    val productSku = MutableStateFlow("")
    val productDesc = MutableStateFlow("")
    val saving = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    fun addUnit() {
        _units.value.add(UnitForm())
        _units.value = _units.value
    }

    fun removeUnit(id: String) {
        _units.value.removeAll { it.id == id }
        if (_units.value.isEmpty()) _units.value.add(UnitForm())
        _units.value = _units.value
    }

    fun saveProduct() {
        val name = productName.value.trim()
        if (name.isEmpty()) {
            message.value = "الرجاء إدخال اسم الصنف"
            return
        }
        val parsedUnits = mutableListOf<Pair<ProductUnit, ProductPrice>>()
        for (u in units.value) {
            val uname = u.name.trim()
            if (uname.isEmpty()) { message.value = "ادخل اسم لكل وحدة"; return }
            val mult = u.multiplier.toLongOrNull()
            if (mult == null || mult < 1) { message.value = "المعامل يجب أن يكون عدد صحيح >= 1"; return }
            val price = u.price.toDoubleOrNull()
            if (price == null || price < 0.0) { message.value = "السعر يجب أن يكون رقم >= 0"; return }
            val unitId = u.id
            val productUnit = ProductUnit(id = unitId, productId = "", name = uname, multiplierToBase = mult)
            val priceRow = ProductPrice(id = UUID.randomUUID().toString(), productUnitId = unitId, price = price, createdAt = System.currentTimeMillis())
            parsedUnits.add(productUnit to priceRow)
        }

        viewModelScope.launch {
            saving.value = true
            try {
                val now = System.currentTimeMillis()
                val productId = UUID.randomUUID().toString()
                val product = Product(id = productId, name = name, sku = productSku.value.trim().ifEmpty { null }, description = productDesc.value.trim().ifEmpty { null }, createdAt = now)
                productDao.insertProduct(product)
                for ((unit, priceRow) in parsedUnits) {
                    val toSaveUnit = unit.copy(productId = productId)
                    productDao.insertUnit(toSaveUnit)
                    val toSavePrice = priceRow.copy(productUnitId = toSaveUnit.id)
                    priceDao.insertPrice(toSavePrice)
                }
                message.value = "تم حفظ الصنف بنجاح"
                productName.value = ""
                productSku.value = ""
                productDesc.value = ""
                _units.value = mutableListOf(UnitForm())
            } catch (e: Exception) {
                message.value = "خطأ عند الحفظ: ${e.message}"
            } finally {
                saving.value = false
            }
        }
    }
}
