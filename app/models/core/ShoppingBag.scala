package models.core

/** Author: Ievgeniia Ozirna
  *
  * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
  */

import java.util.UUID

case class ShoppingBag(
  id: UUID,
  addedItems: Seq[Item],
  total: Double
)

object ShoppingBag {
  def createShoppingBag(item: Item, total: Double): ShoppingBag = ShoppingBag(
    UUID.randomUUID(),
    Seq(item),
    total
  )
}

case class OrderNumber(orderNumber: UUID)
