package exercies.chapter1

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean

class CashRegisterTest : FunSpec(
    {
        context("No item purchased, costs nothing") {
            checkAll(Arb.itemConfiguration()) { config ->
                CashRegister.of(config).checkout(emptyList()) shouldBe PayableAmount(0.0)
            }
        }

        context("Uses discount when purchasing the amount to get") {
            checkAll(
                Arb.itemConfiguration(),
                Exhaustive.boolean()
            ) { (item, price, discount), randomItemIncluded ->
                val randomItem = Item("jukebox")

                val register = CashRegister(item, price, discount) + ItemConfiguration(
                    randomItem,
                    PayableAmount(0.1),
                    Discount(1, 1)
                )

                val purchasesToUsePromotion = (1..discount.get).map { item }

                // Non-promotion item in the middle of things should not affect other promotions
                val purchases =
                    if (!randomItemIncluded) purchasesToUsePromotion
                    else listOf(item, randomItem) + item * (discount.get - 1)

                register.checkout(purchases) shouldBe PayableAmount(discount.payFor * price.value + if (randomItemIncluded) 0.1 else 0.0)
            }
        }

        context("Buy less than promotion, pay full price") {
            checkAll(Arb.itemConfiguration()) { (item, price, discount) ->
                CashRegister(item, price, discount)
                    .checkout(item * (discount.get - 1)) shouldBe PayableAmount((discount.get - 1) * price.value)
            }
        }

        context("Use promotion and then buy one extra") {
            checkAll(Arb.itemConfiguration()) { (item, price, discount) ->
                CashRegister(item, price, discount)
                    .checkout(item * (discount.get + 1)) shouldBe PayableAmount((discount.payFor + 1) * price.value)
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

fun Arb.Companion.item(excludedItems: Set<Item> = emptySet()) = Arb.of(items - excludedItems)
fun Arb.Companion.price() = Arb.double(0.1, 100.0).map { PayableAmount(it) }
fun Arb.Companion.discount() = arbitrary { rs ->
    val payFor = rs.random.nextInt(1, 20)
    val get = rs.random.nextInt(payFor + 1, 40)
    Discount(get, payFor)
}

fun Arb.Companion.itemConfiguration() =
    Arb.bind(Arb.item(), Arb.price(), Arb.discount()) { item, price, discount ->
        ItemConfiguration(item, price, discount)
    }
