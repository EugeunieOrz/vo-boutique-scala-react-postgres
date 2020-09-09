package models.core.tables

import org.joda.time.DateTime
import play.api.libs.json.JsValue

case class DbUserRegistration(
  userId: String,
  lang: String,
  ip: String,
  host: Option[String],
  userAgent: Option[String],
  dateTime: String
)

case class DbUserSettings(
  userId: String,
  lang: String,
  timeZone: Option[String]
)

case class DbUserAgeLimit(
  userId: String,
  ageLimit: Boolean,
  ageLimitCondition: String = "Please confirm that you are over the age of 18."
)

case class DbUserPasswordSurvey(
  userId: String,
  reasons: List[String]
)

case class DbUserLoginAttempts(
  userId: String,
  attempts: JsValue
)

case class DbUserNewsletter(
  userId: String,
  newsletterFashion: JsValue,
  newsletterFineJewelry: JsValue,
  newsletterHomeCollection: JsValue
)

case class DbUserAddresses(
  userId: String,
  addresses: JsValue
)

case class DbUserCreditCards(
  userId: String,
  creditCards: JsValue
)

case class DbUserWishlist(
  userId: String,
  wishlist: JsValue
)

case class DbUserOrders(
  userId: String,
  orders: JsValue
)

case class DbUserShoppingBag(
  userId: String,
  shoppingBag: JsValue
)

case class DbUserLastItemAlerts(
  userId: String,
  alerts: JsValue
)
