package exercies.chapter1

@JvmInline
value class Item(val value: String)

@JvmInline
value class PayableAmount(val value: Double)

operator fun PayableAmount.plus(other: PayableAmount) = PayableAmount(value + other.value)

data class Discount(
    val get: Int,
    val payFor: Int,
)

class CashRegister(
    private val prices: Map<Item, PayableAmount>,
    private val discounts: Map<Item, Discount>
) {

    fun checkout(items: List<Item>): PayableAmount =
        items
            .groupBy { it }
            .mapValues { (item, purchases) -> timesToPayFor(item, purchases.size) }
            .map { (item, toPayFor) -> PayableAmount(toPayFor * prices[item]!!.value) }
            .fold(PayableAmount(0.0), PayableAmount::plus)

    private fun timesToPayFor(item: Item, wanted: Int): Int {
        val discount = discounts[item]!!
        val timesToUsePromotion = wanted / discount.get
        val purchasesWithoutPromotion = wanted % discount.get

        return timesToUsePromotion * discount.payFor + purchasesWithoutPromotion
    }
}
