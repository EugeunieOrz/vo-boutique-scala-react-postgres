package models.core

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import java.util.UUID

case class Color(
  color: String,
  imgIndex: String
)
case class Composition(
  fabric: String,
  percentage: String
)

case class ItemComposition(
  name: String = "Composition",
  content: Seq[Composition]
)

case class Size(
  number: String,
  quantity: Int,
  availability: String = "Available to be shipped by 12/12/2021"
)

case class ItemSize(
  name: String = "Size",
  content: Seq[Size]
)

case class AddItem(
  product: Item,
  size: String,
  quantity: Int,
  userID: UUID
)

case class AddItemGuest(
  product: Item,
  size: String,
  shoppingBag: ShoppingBag
)

case class AddFirstItemGuest(
  product: Item,
  size: String,
  quantity: Int
)

case class EditItemSizes(
  itemID: UUID,
  sizeToAdd: String,
  sizeToRemove: String,
  qty: Int,
  userID: UUID
)

case class EditItemSizesGuest(
  itemID: UUID,
  sizeToAdd: String,
  sizeToRemove: String,
  qty: Int,
  shoppingBag: Option[ShoppingBag]
)

case class EditItemQty(
  itemID: UUID,
  size: String,
  qtyToAdd: Int,
  userID: UUID
)

case class EditItemQtyGuest(
  itemID: UUID,
  size: String,
  qtyToAdd: Int,
  shoppingBag: Option[ShoppingBag]
)

case class RemoveItem(
  itemID: UUID,
  size: String,
  userID: UUID
)

case class RemoveItemGuest(
  itemID: UUID,
  size: String,
  shoppingBag: Option[ShoppingBag]
)

case class WishItem(
  product: Item,
  size: String,
  userID: UUID
)

case class WishItem2(
  productID: UUID,
  size: String,
  userID: UUID
)

case class ShoppingID(
  id: UUID
)
