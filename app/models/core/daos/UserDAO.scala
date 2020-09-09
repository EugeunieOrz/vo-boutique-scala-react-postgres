package models.core.daos

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import models.core.{
  Address,
  AgeLimit,
  CreditCard,
  Settings,
  Registration,
  User,
  UserAddressesList,
  UserCreditCardsList,
  UserLoginAttempts,
  UserLoginAttemptsList,
  Item,
  LoginAttempt,
  LastItemAlert,
  Newsletter,
  NewsletterFashion,
  NewsletterFineJewelry,
  NewsletterHomeCollection,
  Order,
  ShoppingBag,
  UserWishlist,
  UserOrders,
  UserShoppingBag,
  UserLastItemAlertList
}
import models.core.tables._
import util.MyPostgresDriver
import utils.formats.Formats._
import utils.core.CustomerExecutionContext

import scala.concurrent.Future

import org.joda.time.DateTime

import com.mohiva.play.silhouette.api.LoginInfo

import play.api.libs.json._
import play.api.i18n.Lang
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}

/**
 * Give access to the user object.
 */
@Singleton
class UserDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: CustomerExecutionContext) extends UserComponent with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val loginInfos = TableQuery[LoginInfoTable]
  private val users = TableQuery[UserTable]
  private val userRegistration = TableQuery[UserRegistrationTable]
  private val userSettings = TableQuery[UserSettingsTable]
  private val userAgeLimit = TableQuery[UserAgeLimitTable]
  private val userPasswordSurvey = TableQuery[UserPasswordSurveyTable]
  private val userLoginAttempts = TableQuery[UserLoginAttemptsTable]
  private val userNewslettersTable = TableQuery[UserNewsletterTable]
  private val userAddressesTable = TableQuery[UserAddressesTable]
  private val userCreditCardsTable = TableQuery[UserCreditCardsTable]
  private val userOrdersTable = TableQuery[UserOrdersTable]
  private val userWishlistTable = TableQuery[UserWishlistTable]
  private val userShoppingBagTable = TableQuery[UserShoppingBagTable]
  private val userLastItemAlertsTable = TableQuery[UserLastItemAlertsTable]

  def loginInfoQuery(loginInfo: LoginInfo): Query[LoginInfoTable, DbLoginInfo, Seq] = {
    loginInfos.filter(dbLoginInfo => dbLoginInfo.providerId === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
  }

  def validateUserLoginAttempts(json: JsValue): Option[Seq[UserLoginAttempts]] =
    json.validate[UserLoginAttemptsList] match {
      case JsSuccess(value, _) => Some(value.attempts)
      case e: JsError => None
    }

  def validateUserAddresses(json: JsValue): Option[Seq[Address]] =
    json.validate[UserAddressesList] match {
      case JsSuccess(value, _) => Some(value.addresses)
      case e: JsError => None
    }

  def validateUserCreditCards(json: JsValue): Option[Seq[CreditCard]] =
    json.validate[UserCreditCardsList] match {
      case JsSuccess(value, _) => Some(value.creditCards)
      case e: JsError => None
    }

  def validateUserWishlist(json: JsValue): Option[Seq[Item]] =
    json.validate[UserWishlist] match {
      case JsSuccess(value, _) => Some(value.wishlist)
      case e: JsError => None
    }

  def validateUserOrders(json: JsValue): Option[Seq[Order]] =
    json.validate[UserOrders] match {
      case JsSuccess(value, _) => Some(value.orders)
      case e: JsError => None
    }

  def validateUserShoppingBag(json: JsValue): Option[ShoppingBag] =
    json.validate[UserShoppingBag] match {
      case JsSuccess(value, _) => Some(value.shoppingBag)
      case e: JsError => None
    }

  def validateUserLastItemAlerts(json: JsValue): Option[Seq[LastItemAlert]] =
    json.validate[UserLastItemAlertList] match {
      case JsSuccess(value, _) => Some(value.alerts)
      case e: JsError => None
    }


  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {

    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUser <- users.filter(_.userID === dbLoginInfo.userId)
      dbRegistration <- userRegistration.filter(_.userId === dbUser.userID)
      dbSettings <- userSettings.filter(_.userId === dbUser.userID)
      dbAgeLimit <- userAgeLimit.filter(_.userId === dbUser.userID)
      dbPS <- userPasswordSurvey.filter(_.userId === dbUser.userID)
      dbUserLoginAttempts <- userLoginAttempts.filter(_.userId === dbUser.userID)
      dbUserNewsletters <- userNewslettersTable.filter(_.userId === dbUser.userID)
      dbUserAddresses <- userAddressesTable.filter(_.userId === dbUser.userID)
      dbUserCards <- userCreditCardsTable.filter(_.userId === dbUser.userID)
      dbUserOrders <- userOrdersTable.filter(_.userId === dbUser.userID)
      dbUserWishlist <- userWishlistTable.filter(_.userId === dbUser.userID)
      dbUserShoppingBag <- userShoppingBagTable.filter(_.userId === dbUser.userID)
      dbUserAlerts <- userLastItemAlertsTable.filter(_.userId === dbUser.userID)
    } yield (
      dbUser, dbLoginInfo, dbRegistration, dbSettings, dbAgeLimit, dbPS,
      dbUserLoginAttempts, dbUserNewsletters, dbUserAddresses, dbUserCards,
      dbUserOrders, dbUserWishlist, dbUserShoppingBag, dbUserAlerts
    )

    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map {
        case (
          user,
          loginInfo,
          registration,
          settings,
          ageLimit,
          passwordSurvey,
          userLogins,
          userNewsletters,
          userAddresses,
          userCards,
          userOrders,
          userWishlist,
          userShoppingBag,
          userAlerts
        ) => User(
          UUID.fromString(user.userID),
          LoginInfo(loginInfo.providerId, loginInfo.providerKey),
          user.title,
          user.firstName,
          user.lastName,
          user.email,
          user.accountStatus,
          Registration(
            Lang(registration.lang),
            registration.ip,
            registration.host,
            registration.userAgent,
            DateTime.parse(registration.dateTime)
          ),
          Settings(
            Lang(settings.lang),
            settings.timeZone
          ),
          AgeLimit(ageLimit.ageLimit),
          user.dateOfBirth,
          Some(passwordSurvey.reasons),
          validateUserLoginAttempts(userLogins.attempts),
          validateUserAddresses(userAddresses.addresses),
          validateUserCreditCards(userCards.creditCards),
          Newsletter(
            userNewsletters.newsletterFashion.validate[NewsletterFashion] match {
              case JsSuccess(value, _) => NewsletterFashion(value.isChecked)
              case e: JsError => NewsletterFashion(false)
            },
            userNewsletters.newsletterFineJewelry.validate[NewsletterFineJewelry] match {
              case JsSuccess(value, _) => NewsletterFineJewelry(value.isChecked)
              case e: JsError => NewsletterFineJewelry(false)
            },
            userNewsletters.newsletterHomeCollection.validate[NewsletterHomeCollection] match {
              case JsSuccess(value, _) => NewsletterHomeCollection(value.isChecked)
              case e: JsError => NewsletterHomeCollection(false)
            }
          ),
          validateUserWishlist(userWishlist.wishlist),
          validateUserOrders(userOrders.orders),
          validateUserShoppingBag(userShoppingBag.shoppingBag),
          validateUserLastItemAlerts(userAlerts.alerts)
        )
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User]] = {

      val userQuery = for {
        dbUser <- users.filter(_.userID === userID.toString)
        dbLoginInfo <- loginInfos.filter(_.userId === userID.toString)
        dbRegistration <- userRegistration.filter(_.userId === dbUser.userID)
        dbSettings <- userSettings.filter(_.userId === dbUser.userID)
        dbAgeLimit <- userAgeLimit.filter(_.userId === dbUser.userID)
        dbPS <- userPasswordSurvey.filter(_.userId === dbUser.userID)
        dbUserLoginAttempts <- userLoginAttempts.filter(_.userId === dbUser.userID)
        dbUserNewsletters <- userNewslettersTable.filter(_.userId === dbUser.userID)
        dbUserAddresses <- userAddressesTable.filter(_.userId === dbUser.userID)
        dbUserCards <- userCreditCardsTable.filter(_.userId === dbUser.userID)
        dbUserOrders <- userOrdersTable.filter(_.userId === dbUser.userID)
        dbUserWishlist <- userWishlistTable.filter(_.userId === dbUser.userID)
        dbUserShoppingBag <- userShoppingBagTable.filter(_.userId === dbUser.userID)
        dbUserAlerts <- userLastItemAlertsTable.filter(_.userId === dbUser.userID)
      } yield (
        dbUser, dbLoginInfo, dbRegistration, dbSettings, dbAgeLimit, dbPS,
        dbUserLoginAttempts, dbUserNewsletters, dbUserAddresses, dbUserCards,
        dbUserOrders, dbUserWishlist, dbUserShoppingBag, dbUserAlerts
      )

      db.run(userQuery.result.headOption).map { resultOption =>
        resultOption.map {
          case (
            user,
            loginInfo,
            registration,
            settings,
            ageLimit,
            passwordSurvey,
            userLogins,
            userNewsletters,
            userAddresses,
            userCards,
            userOrders,
            userWishlist,
            userShoppingBag,
            userAlerts
          ) => User(
            UUID.fromString(user.userID),
            LoginInfo(loginInfo.providerId, loginInfo.providerKey),
            user.title,
            user.firstName,
            user.lastName,
            user.email,
            user.accountStatus,
            Registration(
              Lang(registration.lang),
              registration.ip,
              registration.host,
              registration.userAgent,
              DateTime.parse(registration.dateTime)
            ),
            Settings(
              Lang(settings.lang),
              settings.timeZone
            ),
            AgeLimit(ageLimit.ageLimit),
            user.dateOfBirth,
            Some(passwordSurvey.reasons),
            validateUserLoginAttempts(userLogins.attempts),
            validateUserAddresses(userAddresses.addresses),
            validateUserCreditCards(userCards.creditCards),
            Newsletter(
              userNewsletters.newsletterFashion.validate[NewsletterFashion] match {
                case JsSuccess(value, _) => NewsletterFashion(value.isChecked)
                case e: JsError => NewsletterFashion(false)
              },
              userNewsletters.newsletterFineJewelry.validate[NewsletterFineJewelry] match {
                case JsSuccess(value, _) => NewsletterFineJewelry(value.isChecked)
                case e: JsError => NewsletterFineJewelry(false)
              },
              userNewsletters.newsletterHomeCollection.validate[NewsletterHomeCollection] match {
                case JsSuccess(value, _) => NewsletterHomeCollection(value.isChecked)
                case e: JsError => NewsletterHomeCollection(false)
              }
            ),
            validateUserWishlist(userWishlist.wishlist),
            validateUserOrders(userOrders.orders),
            validateUserShoppingBag(userShoppingBag.shoppingBag),
            validateUserLastItemAlerts(userAlerts.alerts)
          )
        }
      }

  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] = {

    val dbUser = DbUser(
      user.userID.toString,
      user.title,
      user.firstName,
      user.lastName,
      user.email,
      user.accountStatus,
      user.dateOfBirth
    )
    val dbLoginInfo = DbLoginInfo(user.userID.toString, user.loginInfo.providerID, user.loginInfo.providerKey)

    val dbUserRegistration = DbUserRegistration(
      user.userID.toString,
      user.registration.lang.code,
      user.registration.ip,
      user.registration.host,
      user.registration.userAgent,
      user.registration.dateTime.toString
    )

    val dbUserSettings = DbUserSettings(
      user.userID.toString,
      user.settings.lang.code,
      user.settings.timeZone
    )

    val dbUserAgeLimit = DbUserAgeLimit(
      user.userID.toString,
      user.ageLimit.ageLimit,
      user.ageLimit.ageLimitCondition,
    )

    val dbUserPasswordSurvey = DbUserPasswordSurvey(
      user.userID.toString,
      user.passwordSurvey match {
        case Some(ps) => ps
        case None => List.empty
      }
    )

    val dbUserLogins = DbUserLoginAttempts(
      user.userID.toString,
      user.loginAttempts match {
        case Some(la) => Json.toJson(
          UserLoginAttemptsList(
            user.userID.toString,
            la
          )
        )
        case None => Json.toJson("")
      }
    )

    val dbUserAddresses = DbUserAddresses(
      user.userID.toString,
      user.addressBook match {
        case Some(addresses) => Json.toJson(
          UserAddressesList(
            user.userID.toString,
            addresses
          )
        )
        case None => Json.toJson("")
      }
    )

    val dbUserCreditCards = DbUserCreditCards(
      user.userID.toString,
      user.cardWallet match {
        case Some(creditCards) => Json.toJson(
          UserCreditCardsList(
            user.userID.toString,
            creditCards
          )
        )
        case None => Json.toJson("")
      }
    )

    val dbUserNewsletter = DbUserNewsletter(
      user.userID.toString,
      Json.toJson(user.newsletters.newsletterFashion),
      Json.toJson(user.newsletters.newsletterFineJewelry),
      Json.toJson(user.newsletters.newsletterHomeCollection)
    )

    val dbUserWishlist = DbUserWishlist(
      user.userID.toString,
      user.wishlist match {
        case Some(w) => Json.toJson(
          UserWishlist(
            user.userID.toString,
            w
          )
        )
        case None => Json.toJson("")
      }
    )

    val dbUserOrders = DbUserOrders(
      user.userID.toString,
      user.orders match {
        case Some(orders) => Json.toJson(
          UserOrders(
            user.userID.toString,
            orders
          )
        )
        case None => Json.toJson("")
      }
    )

    val dbUserShoppingBag = DbUserShoppingBag(
      user.userID.toString,
      user.shoppingBag match {
        case Some(bag) => Json.toJson(
          UserShoppingBag(
            user.userID.toString,
            bag
          )
        )
        case None => Json.toJson("")
      }
    )

    val dbUserAlerts = DbUserLastItemAlerts(
      user.userID.toString,
      user.notifications match {
        case Some(alerts) => Json.toJson(
          UserLastItemAlertList(
            user.userID.toString,
            alerts
          )
        )
        case None => Json.toJson("")
      }
    )

    val actions = (for {
      _ <- userRegistration.insertOrUpdate(dbUserRegistration)
      _ <- userSettings.insertOrUpdate(dbUserSettings)
      _ <- userAgeLimit.insertOrUpdate(dbUserAgeLimit)
      _ <- users.insertOrUpdate(dbUser)
      _ <- userLoginAttempts.insertOrUpdate(dbUserLogins)
      _ <- userPasswordSurvey.insertOrUpdate(dbUserPasswordSurvey)
      _ <- userNewslettersTable.insertOrUpdate(dbUserNewsletter)
      _ <- userAddressesTable.insertOrUpdate(dbUserAddresses)
      _ <- userCreditCardsTable.insertOrUpdate(dbUserCreditCards)
      _ <- loginInfos.insertOrUpdate(dbLoginInfo)
      _ <- userOrdersTable.insertOrUpdate(dbUserOrders)
      _ <- userWishlistTable.insertOrUpdate(dbUserWishlist)
      _ <- userShoppingBagTable.insertOrUpdate(dbUserShoppingBag)
      _ <- userLastItemAlertsTable.insertOrUpdate(dbUserAlerts)
    } yield ())

    db.run(actions).map(_ => user)
  }

  /**
   * Deletes a user's account.
   *
   * @param id The id of the user.
   * @return A future to wait for the process to be completed.
   */
  def removeAccount(id: UUID): Future[Int] = db.run(users.filter(_.userID === id.toString).delete)
}
