package exercies.chapter1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean

class CashRegisterTest : FunSpec(
    {
        fun toCashRegister(item: Item, price: PayableAmount, discount: Discount) =
            CashRegister(mapOf(item to price), mapOf(item to discount))

        context("No item purchased, costs nothing") {
            checkAll(Arb.item(), Arb.price(), Arb.discount()) { item, price, discount ->
                toCashRegister(item, price, discount)
                    .checkout(emptyList()) shouldBe PayableAmount(0.0)
            }
        }

        context("Uses discount when purchasing the amount to get") {
            checkAll(
                Arb.item(),
                Arb.price(),
                Arb.discount(),
                Exhaustive.boolean()
            ) { item, price, discount, randomItemIncluded ->
                val randomItem = Item("jukebox")
                val register = CashRegister(
                    mapOf(item to price, randomItem to PayableAmount(0.1)),
                    mapOf(item to discount, randomItem to Discount(1, 1))
                )

                val purchasesToUsePromotion = (1..discount.get).map { item }

                // Non-promotion item in the middle of things should not affect other promotions
                val purchases =
                    if (!randomItemIncluded) purchasesToUsePromotion
                    else listOf(item, randomItem) + (2..discount.get).map { item }

                register.checkout(purchases) shouldBe PayableAmount(discount.payFor * price.value + if (randomItemIncluded) 0.1 else 0.0)
            }
        }

        context("Buy less than promotion, pay full price") {
            checkAll(Arb.item(), Arb.price(), Arb.discount()) { item, price, discount ->
                toCashRegister(item, price, discount)
                    .checkout((2..discount.get).map { item }) shouldBe PayableAmount((discount.get - 1) * price.value)
            }
        }

        context("Use promotion and then buy one extra") {
            checkAll(Arb.item(), Arb.price(), Arb.discount()) { item, price, discount ->
                toCashRegister(item, price, discount)
                    .checkout((0..discount.get).map { item }) shouldBe PayableAmount((discount.payFor + 1) * price.value)
            }
        }
    }
)

val items =
    listOf(
        Item("milk"),
        Item("eggs"),
        Item("cheese"),
        Item("coffee"),
        Item("mustard"),
        Item("ham"),
    )

fun Arb.Companion.item() = Arb.of(items)
fun Arb.Companion.price() = Arb.double(0.1, 100.0).map { PayableAmount(it) }
fun Arb.Companion.discount() = arbitrary { rs ->
    val payFor = rs.random.nextInt(1, 20)
    val get = rs.random.nextInt(payFor + 1, 40)
    Discount(get, payFor)
}
