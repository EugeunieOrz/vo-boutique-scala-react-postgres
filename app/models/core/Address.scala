package models.core

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

/**
 * The Address object.
 *
 * @param id           The unique ID of the user.
 */
case class Address(
  firstName: String,
  lastName: String,
  addInf: AdditionalInfo,
  address: String,
  zipCode: String,
  city: String,
  country: String,
  state: String,
  email: String,
  dayTel: TelephoneDay,
  eveningTel: TelephoneEvening,
  mark1: DefaultShippingAddressMark,
  mark2: BillingAddressMark
)

case class TelephoneDay(
  val name: String = "Telephone (day)",
  telephone: String
)

case class TelephoneEvening(
  val name: String = "Telephone (evening)",
  telephone: Option[String]
)

case class AdditionalInfo(
  val name: String = "Additional information for delivery",
  descr: Option[String]
)

case class DefaultShippingAddressMark(
  checked: Boolean,
  val description: String = "Mark as default shipping address"
)

case class BillingAddressMark(
  checked: Boolean,
  val description: String = "Save as preferred billing address in My Account"
)

case class AddNewAddressData(
  userID: String,
  countryByIP: String,
  data: AddNewAddress
)

case class EditAddressData(
  userID: String,
  index: Int,
  countryByIP: String,
  data: AddNewAddress
)

case class AddNewAddress(
  firstName: String,
  lastName: String,
  additional: Option[String],
  address: String,
  zipCode: String,
  city: String,
  country: String,
  province: String,
  email: String,
  dayTelephone: String,
  eveningTelephone: Option[String],
  defShipAddr: Boolean,
  preferBillAddr: Boolean
)

case class ShippingAddressAndPaymentDetails(
  firstName: String,
  lastName: String,
  additional: String,
  address: String,
  zipCode: String,
  city: String,
  country: String,
  province: String,
  email: String,
  telephone: String,
  code: String,
  name: String
)
