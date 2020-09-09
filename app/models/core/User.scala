package models.core

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import java.util.UUID
import scala.collection.immutable.List
import org.joda.time.DateTime

/**
 * The user object.
 *
 * @param userID           The unique ID of the user.
 * @param loginInfo    The login Info of the user.
 * @param title        The title of the user.
 * @param firstName    The first name of the user.
 * @param lastName     The last name of the user.
 * @param name         Maybe the name of the authenticated user.
 * @param email        Maybe the email of the authenticated provider.
 * @param registration The registration data.
 * @param settings     The user settings.
 * @param dateOfBirth  The date of birth of the user.
 * @param passwordSurvey The password survey data.
 */
case class User(
  userID: UUID,
  loginInfo: LoginInfo,
  title: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  accountStatus: Option[String],
  registration: Registration,
  settings: Settings,
  ageLimit: AgeLimit,
  dateOfBirth: Option[String],
  passwordSurvey: Option[List[String]],
  loginAttempts: Option[Seq[UserLoginAttempts]],
  addressBook: Option[Seq[Address]],
  cardWallet: Option[Seq[CreditCard]],
  newsletters: Newsletter,
  wishlist: Option[Seq[Item]],
  orders: Option[Seq[Order]],
  shoppingBag: Option[ShoppingBag],
  notifications: Option[Seq[LastItemAlert]]
) extends Identity

case class UserID(
  id: UUID
)

case class AgeLimit(
  ageLimit: Boolean = false,
  ageLimitCondition: String = "Please confirm that you are over the age of 18."
)

case class LoginEmail(
  email: String
)

case class UserAddressesList(
  userId: String,
  addresses: Seq[Address]
)

case class UserCreditCardsList(
  userId: String,
  creditCards: Seq[CreditCard]
)

case class UserLoginAttemptsList(
  userId: String,
  attempts: Seq[UserLoginAttempts]
)

case class UserWishlist(
  userId: String,
  wishlist: Seq[Item]
)

case class UserOrders(
  userId: String,
  orders: Seq[Order]
)

case class UserShoppingBag(
  userId: String,
  shoppingBag: ShoppingBag
)

case class UserLastItemAlertList(
  userId: String,
  alerts: Seq[LastItemAlert]
)

case class UserLoginAttempts(
  id: UUID,
  dateTime: DateTime,
  emailLoginAttemptSent: Boolean,
  emailAccountLockedSent: Boolean,
  loginAttempts: Seq[LoginAttempt]
)

case class LoginAttempt(
  dateTime: DateTime,
  count: Long
)

case class PasswordSurvey(
  userID: UUID,
  reason: String
)
