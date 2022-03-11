package exercies.chapter1

@JvmInline
value class Item(val value: String)
operator fun Item.times(count: Int) = (1..count).map { this }

@JvmInline
value class PayableAmount(val value: Double)

operator fun PayableAmount.plus(other: PayableAmount) = PayableAmount(value + other.value)

data class Discount(
    val get: Int,
    val payFor: Int,
)

data class ItemConfiguration(
    val item: Item,
    val price: PayableAmount,
    val discount: Discount,
)

class CashRegister(
    private val prices: Map<Item, PayableAmount>,
    private val discounts: Map<Item, Discount>
) {
    companion object {
        fun empty() = CashRegister(emptyMap(), emptyMap())
        fun of(vararg itemConfiguration: ItemConfiguration) = itemConfiguration.fold(empty(), CashRegister::plus)
    }

    constructor(item: Item, price: PayableAmount, discount: Discount) : this(
        mapOf(item to price),
        mapOf(item to discount)
    )

    operator fun plus(config: ItemConfiguration) = CashRegister(
        prices + listOf(config.item to config.price),
        discounts + listOf(config.item to config.discount)
    )

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
