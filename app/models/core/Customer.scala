package models.core

/** Author: Ievgeniia Ozirna
  *
  * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
  */

import java.time.Instant
import java.util.UUID

case class Order(
  id: UUID,
  dateTime: Option[Instant],
  countryByIP: Option[CountryAtPurchase],
  shoppingBag: Option[ShoppingBag],
  status: String
)

case class Refund(
  id: UUID,
  dateTime: Instant,
  order: Order,
  reasonToRefund: String
)

case class OrderToReturn(
  id: UUID,
  data: ReturnData,
  items: Seq[String]
)

case class OrderToReturn2(
  id: UUID,
  data: ReturnData,
  items: Seq[String],
  userID: UUID
)

case class ReturnData(
  firstName: String,
  lastName: String,
  email: String,
  telephone: String,
  reasonForRefund: String
)

case class Customer(
  id: UUID,
  dateTime: Instant,
  registration: Registration,
  shoppingBag: ShoppingBag,
)
case class SignIn(
  email: String,
  password: String,
  rememberMe: Boolean
)
case class SignInToShop(
  data: SignIn,
  shoppingBag: ShoppingBag
)
case class SignUpToShop(
  data: SignUpEmail,
  shoppingBag: ShoppingBag
)

case class SignUpEmail(
  title: String,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  newsletterFashion: Boolean,
  newsletterFineJewelry: Boolean,
  newsletterHomeCollection: Boolean,
  ageLimit: Boolean
)

case class CompleteSignIn(
  data: PasswordData,
  email: String,
  shoppingBag: ShoppingBag
)

case class CreateAccount(
  data: AccountData,
  shoppingBag: ShoppingBag
)

case class PasswordData(
  password: String
)

case class AccountData(
  title: String,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  ageLimit: Boolean
)
