package controllers.account

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import forms.account.AddNewCardForm
import models.core.{
  BillingAddress,
  CardNumberEncrypted,
  CreditCard,
  PreferredCreditCard,
  StateOrProvince,
  UserID,
  User
}
import models.core.services.{ CreditCardEncryptionService, UserService }
import utils.formats.Formats._
import utils.core.JSRouter
import utils.core.SomeMethods._
import utils.silhouette.{ BoutiqueEnv, CustomerController }

import net.ceedubs.ficus.Ficus._
import org.joda.time.DateTime

import play.api.i18n.{ Messages, MessagesProvider }
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Details` controller.
 *
 * @param controllerComponents  The Play controller components.
 * @param silhouette            The Silhouette stack.
 * @param userService           The user service implementation.
 * @param ex                    The execution context.
 */
class CardWalletController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
//  creditCardEncryptionService: CreditCardEncryptionService,
  userService: UserService
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  /**
   * Adds a new card to the user's card wallet in My Account.
   *
   * @return A Play result.
   */
  def addNewCard(userID: String, cardType: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      AddNewCardForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => userService.retrieve(UUID.fromString(userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val l = data.cardNumber.length
    //        val first = data.cardNumber.substring(0, l - 4)
    //        val lastDigits = data.cardNumber.substring(l - 4)
    //        val cardNumEncrypted = creditCardEncryptionService.encrypt(data.cardNumber.substring(0, l - 4))

            val newCard = CreditCard(
              cardType = cardType,
              cardNumber = data.cardNumber,
        /**      cardNumber = CardNumberEncrypted(
                cardNumber = cardNumEncrypted._1,
                lastDigits = lastDigits,
                nonce = cardNumEncrypted._2
              ), */
              expMonth = data.month,
              expYear = data.year,
              address = BillingAddress(
                firstName = data.firstName,
                lastName = data.lastName,
                address = data.address,
                zipCode = data.zipCode,
                city = data.city,
                country = data.country,
                state = StateOrProvince(
                  content = data.province
                )
              ),
              prefCrdCard = PreferredCreditCard(
                mark = data.prefCrdCard
              )
            )
            user.cardWallet match {
              case Some(cards) =>
                val newSeq = cards :+ newCard
                userService.save(
                  user.copy(
                    cardWallet = Some(newSeq)
                  )
                ).map { usr =>
                    Ok(ApiResponse(
                      "account.cardwallet.updated",
                      Messages("account.cardwallet.updated"),
                      Json.toJson(usr)))
                  }
              case None => userService.save(
                user.copy(
                  cardWallet = Some(Seq(newCard))
                )
              ).map { usr =>
                  Ok(ApiResponse(
                    "account.cardwallet.updated",
                    Messages("account.cardwallet.updated"),
                    Json.toJson(usr)))
                }
            }

          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }.recover {
          case _: ProviderException =>
            BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
        }
      )
    }

  /**
   * Edits credit card in the user's card wallet in My Account.
   *
   * @return A Play result.
   */
  def editCard(userID: String, index: Int, cardType: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      AddNewCardForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => userService.retrieve(UUID.fromString(userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
      /**      val l = data.cardNumber.length
            val first = data.cardNumber.substring(0, l - 4)
            val lastDigits = data.cardNumber.substring(l - 4)
            val cardNumEncrypted = creditCardEncryptionService.encrypt(data.cardNumber.substring(0, l - 4)) */
            val newCard = CreditCard(
              cardType = cardType,
              cardNumber = data.cardNumber,
          /**    cardNumber = CardNumberEncrypted(
                cardNumber = cardNumEncrypted._1,
                lastDigits = lastDigits,
                nonce = cardNumEncrypted._2
              ), */
              expMonth = data.month,
              expYear = data.year,
              address = BillingAddress(
                firstName = data.firstName,
                lastName = data.lastName,
                address = data.address,
                zipCode = data.zipCode,
                city = data.city,
                country = data.country,
                state = StateOrProvince(
                  content = data.province
                )
              ),
              prefCrdCard = PreferredCreditCard(
                mark = data.prefCrdCard
              )
            )
            user.cardWallet match {
              case Some(cards) =>
                val newSeq = cards.updated(index, newCard)
                userService.save(
                  user.copy(
                    cardWallet = Some(newSeq)
                  )
                ).map { usr =>
                    Ok(ApiResponse(
                      "account.cardwallet.updated",
                      Messages("account.cardwallet.updated"),
                      Json.toJson(usr)))
                  }
              case None =>
                userService.save(
                  user.copy(
                    cardWallet = Some(Seq(newCard))
                  )
                ).map { usr =>
                    Ok(ApiResponse(
                      "account.cardwallet.updated",
                      Messages("account.cardwallet.updated"),
                      Json.toJson(usr)))
                  }
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }.recover {
          case _: ProviderException =>
            BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
        }
      )
    }

  /**
   * Removes credit card from the user's card wallet.
   *
   * @return A Play result.
   */
  def removeCard(userID: String, indexToRemoveCard: Int): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      userService.retrieve(UUID.fromString(userID)).flatMap {
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          user.cardWallet match {
            case Some(cards) =>
              val seqUpdated = removeElementFromSeq(cards, indexToRemoveCard)
              userService.save(
                user.copy(
                  cardWallet = Some(seqUpdated)
                )
              ).map { usr =>
                  Ok(ApiResponse(
                    "account.card.deleted",
                    Messages("account.card.deleted"),
                    Json.toJson(usr)))
                }
            case None =>
              Future.successful(BadRequest(ApiResponse(
                "account.details.invalid", Messages("account.details.invalid")
              )))
          }
        case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }.recover {
        case _: ProviderException =>
          BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
      }
    }

}
