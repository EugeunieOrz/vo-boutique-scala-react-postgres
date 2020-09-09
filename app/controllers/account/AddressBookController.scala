package controllers.account

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import com.google.inject.name.Named
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import models.core.{
  AddNewAddressData,
  EditAddressData,
  AdditionalInfo,
  Address,
  BillingAddressMark,
  DefaultShippingAddressMark,
  TelephoneDay,
  TelephoneEvening,
  User
}
import models.core.services.UserService
import utils.core.JSRouter
import utils.core.SomeMethods._
import utils.formats.Formats._
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
class AddressBookController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
  userService: UserService
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  /**
   * Adds a new address to the user's address book in My Account.
   *
   * @return A Play result.
   */
  def addNewAddress: Action[JsValue] =
    SecuredAction(parse.json).async { implicit request =>
      request.body.validate[AddNewAddressData].fold(
        errors => Future.successful(
          BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
        data => userService.retrieve(UUID.fromString(data.userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val newAddress = Address(
              firstName = data.data.firstName,
              lastName = data.data.lastName,
              addInf = AdditionalInfo(
                descr = data.data.additional
              ),
              address = data.data.address,
              zipCode = data.data.zipCode,
              city = data.data.city,
              country = data.data.country,
              state = data.data.province,
              email = data.data.email,
              dayTel = TelephoneDay(
                telephone = data.data.dayTelephone
              ),
              eveningTel = TelephoneEvening(
                telephone = data.data.eveningTelephone
              ),
              mark1 = DefaultShippingAddressMark(
                checked = if (data.data.country == data.countryByIP) data.data.defShipAddr else false
              ),
              mark2 = BillingAddressMark(
                checked = if (data.data.country == data.countryByIP) data.data.preferBillAddr else true
              )
            )
            user.addressBook match {
              case Some(addressesSeq) =>
                val newSeqOfAddresses = newAddress +: addressesSeq
                val updatedUser = user.copy(
                  addressBook = Some(newSeqOfAddresses)
                )
                userService.save(updatedUser).map { usr =>
                  Ok(ApiResponse(
                    "account.address.saved",
                    Messages("account.address.saved"),
                    Json.toJson(updatedUser)))
                }
              case None =>
                val updatedUser2 = user.copy(
                  addressBook = Some(Seq(newAddress))
                )
                userService.save(updatedUser2).map { usr =>
                  Ok(ApiResponse(
                    "account.address.saved",
                    Messages("account.address.saved"),
                    Json.toJson(updatedUser2)))
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
   * Edits address to the user's address book in My Account.
   *
   * @return A Play result.
   */
  def editAddress: Action[JsValue] =
    SecuredAction(parse.json).async { implicit request =>
      request.body.validate[EditAddressData].fold(
        errors => Future.successful(
          BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
        data => userService.retrieve(UUID.fromString(data.userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val newAddress = Address(
              firstName = data.data.firstName,
              lastName = data.data.lastName,
              addInf = AdditionalInfo(
                descr = data.data.additional
              ),
              address = data.data.address,
              zipCode = data.data.zipCode,
              city = data.data.city,
              country = data.data.country,
              state = data.data.province,
              email = data.data.email,
              dayTel = TelephoneDay(
                telephone = data.data.dayTelephone
              ),
              eveningTel = TelephoneEvening(
                telephone = data.data.eveningTelephone
              ),
              mark1 = DefaultShippingAddressMark(
                checked = if (
                  data.data.country == data.countryByIP || data.countryByIP == "N/A"
                ) data.data.defShipAddr else false
              ),
              mark2 = BillingAddressMark(
                checked = if (
                  data.data.country == data.countryByIP || data.countryByIP == "N/A"
                ) data.data.preferBillAddr else true
              )
            )
            user.addressBook match {
              case Some(addressesSeq) =>
                val newSeqOfAddresses = addressesSeq.updated(data.index, newAddress)
                val updatedUser = user.copy(
                  addressBook = Some(newSeqOfAddresses)
                )
                userService.save(updatedUser).map { usr =>
                  Ok(ApiResponse(
                    "account.address.saved",
                    Messages("account.address.saved"),
                    Json.toJson(updatedUser)))
                }
              case None =>
                val updatedUser2 = user.copy(
                  addressBook = Some(Seq(newAddress))
                )
                userService.save(updatedUser2).map { usr =>
                  Ok(ApiResponse(
                    "account.address.saved",
                    Messages("account.address.saved"),
                    Json.toJson(updatedUser2)))
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
   * Removes address from the user's address book.
   *
   * @return A Play result.
   */
  def removeAddress(userID: String, indexToRemoveAddress: Int): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      userService.retrieve(UUID.fromString(userID)).flatMap {
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          user.addressBook match {
            case Some(addresses) =>
              val seqUpdated = removeElementFromSeq(addresses, indexToRemoveAddress)
              val updatedUser = user.copy(
                addressBook = Some(seqUpdated)
              )
              userService.save(updatedUser).map { usr =>
                Ok(ApiResponse(
                  "account.address.deleted",
                  Messages("account.addresses.deleted"),
                  Json.toJson(updatedUser)))
              }
            case None =>
              Future.successful(BadRequest(ApiResponse(
                "account.details.invalid", Messages("account.details.invalid"))))
          }
        case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }.recover {
        case _: ProviderException =>
          BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
      }
    }

}
