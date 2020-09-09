package models.core.tables

import util.MyPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue

trait UserComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class UserTable(tag: Tag) extends Table[DbUser](tag, "users") {

    def userID: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def title: Rep[Option[String]] = column[Option[String]]("title")
    def firstName: Rep[Option[String]] = column[Option[String]]("first_name")
    def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
    def email: Rep[Option[String]] = column[Option[String]]("email")
    def accountStatus: Rep[Option[String]] = column[Option[String]]("account_status")
    def dateOfBirth: Rep[Option[String]] = column[Option[String]]("date_of_birth")
    def * = (
      userID,
      title,
      firstName,
      lastName,
      email,
      accountStatus,
      dateOfBirth
    ) <> (DbUser.tupled, DbUser.unapply)

  }

  class LoginInfoTable(tag: Tag) extends Table[DbLoginInfo](tag, "login_info") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def providerId: Rep[String] = column[String]("provider_id")
    def providerKey: Rep[String] = column[String]("provider_key")
    def * = (userId, providerId, providerKey) <> (DbLoginInfo.tupled, DbLoginInfo.unapply)

  }

  class UserRegistrationTable(tag: Tag) extends Table[DbUserRegistration](tag, "user_registration") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def lang: Rep[String] = column[String]("lang")
    def ip: Rep[String] = column[String]("ip")
    def host: Rep[Option[String]] = column[Option[String]]("host")
    def userAgent: Rep[Option[String]] = column[Option[String]]("user_agent")
    def dateTime: Rep[String] = column[String]("date_time")

    def * = (userId, lang, ip, host, userAgent, dateTime) <> (DbUserRegistration.tupled, DbUserRegistration.unapply)

  }

  class UserSettingsTable(tag: Tag) extends Table[DbUserSettings](tag, "user_settings") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def lang: Rep[String] = column[String]("lang")
    def timeZone: Rep[Option[String]] = column[Option[String]]("time_zone")

    def * = (userId, lang, timeZone) <> (DbUserSettings.tupled, DbUserSettings.unapply)

  }

  class UserAgeLimitTable(tag: Tag) extends Table[DbUserAgeLimit](tag, "user_age_limit") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def ageLimit: Rep[Boolean] = column[Boolean]("age_limit")
    def ageLimitCondition: Rep[String] = column[String]("age_limit_condition")

    def * = (userId, ageLimit, ageLimitCondition) <> (DbUserAgeLimit.tupled, DbUserAgeLimit.unapply)

  }

  class UserPasswordSurveyTable(tag: Tag) extends Table[DbUserPasswordSurvey](tag, "user_password_survey") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def reasons: Rep[List[String]] = column[List[String]]("reasons")
    def * = (userId, reasons) <> (DbUserPasswordSurvey.tupled, DbUserPasswordSurvey.unapply)

  }

  class UserLoginAttemptsTable(tag: Tag) extends Table[DbUserLoginAttempts](tag, "user_login_attempts") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def attempts: Rep[JsValue] = column[JsValue]("attempts")
    def * = (userId, attempts) <> (DbUserLoginAttempts.tupled, DbUserLoginAttempts.unapply)

  }

  class UserNewsletterTable(tag: Tag) extends Table[DbUserNewsletter](tag, "user_newsletter") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def newsletterFashion: Rep[JsValue] = column[JsValue]("newsletter_fashion")
    def newsletterFineJewelry: Rep[JsValue] = column[JsValue]("newsletter_fine_jewelry")
    def newsletterHomeCollection: Rep[JsValue] = column[JsValue]("newsletter_home_collection")
    def * = (
      userId,
      newsletterFashion,
      newsletterFineJewelry,
      newsletterHomeCollection
    ) <> (DbUserNewsletter.tupled, DbUserNewsletter.unapply)

  }

  class UserAddressesTable(tag: Tag) extends Table[DbUserAddresses](tag, "user_addresses") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def addresses: Rep[JsValue] = column[JsValue]("addresses")
    def * = (userId, addresses) <> (DbUserAddresses.tupled, DbUserAddresses.unapply)

  }

  class UserCreditCardsTable(tag: Tag) extends Table[DbUserCreditCards](tag, "user_credit_cards") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def creditCards: Rep[JsValue] = column[JsValue]("credit_cards")
    def * = (userId, creditCards) <> (DbUserCreditCards.tupled, DbUserCreditCards.unapply)

  }

  class UserWishlistTable(tag: Tag) extends Table[DbUserWishlist](tag, "user_wishlist") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def wishlist: Rep[JsValue] = column[JsValue]("wishlist")
    def * = (userId, wishlist) <> (DbUserWishlist.tupled, DbUserWishlist.unapply)

  }

  class UserOrdersTable(tag: Tag) extends Table[DbUserOrders](tag, "user_orders") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def orders: Rep[JsValue] = column[JsValue]("orders")
    def * = (userId, orders) <> (DbUserOrders.tupled, DbUserOrders.unapply)

  }

  class UserShoppingBagTable(tag: Tag) extends Table[DbUserShoppingBag](tag, "user_shopping_bag") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def shoppingBag: Rep[JsValue] = column[JsValue]("shopping_bag")
    def * = (userId, shoppingBag) <> (DbUserShoppingBag.tupled, DbUserShoppingBag.unapply)

  }

  class UserLastItemAlertsTable(tag: Tag) extends Table[DbUserLastItemAlerts](tag, "user_last_item_alerts") {

    def userId: Rep[String] = column[String]("user_id", O.PrimaryKey)
    def alerts: Rep[JsValue] = column[JsValue]("alerts")
    def * = (userId, alerts) <> (DbUserLastItemAlerts.tupled, DbUserLastItemAlerts.unapply)

  }
}
