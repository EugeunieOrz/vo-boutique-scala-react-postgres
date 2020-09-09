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
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import forms.account.{
  DateOfBirthForm,
  EditEmailForm,
  EditNameForm,
  ChangePasswordForm,
  NewsletterForm
}
import models.core.{
  Newsletter,
  NewsletterFashion,
  NewsletterFineJewelry,
  NewsletterHomeCollection,
  SubscribeToNewsletter,
  User
}
import models.core.services.UserService
import models.newsletter.services.{ NewsletterService, NewsletterTaskService }
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
 * @param authInfoRepository     The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 * @param silhouette            The Silhouette stack.
 * @param userService           The user service implementation.
 * @param ex                    The execution context.
 */
class DetailsController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
  @Named("customer-auth-info-repository") authInfoRepository: AuthInfoRepository,
  newsletterService: NewsletterService,
  newsletterTaskService: NewsletterTaskService,
  passwordHasher: PasswordHasher,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  /**
   * Updates user's date of birth.
   *
   * @return A Play result.
   */
  def updateDetails(userID: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      DateOfBirthForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => userService.retrieve(UUID.fromString(userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val updatedUser = user.copy(
              dateOfBirth = Some(data.bday + "/" + data.bmonth + "/" + data.byear)
            )
            userService.save(updatedUser).map { usr =>
              Ok(ApiResponse(
                "account.details.updated",
                Messages("account.details.updated"),
                Json.toJson(usr)))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }.recover {
          case _: ProviderException =>
            BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
        }
      )
    }

  /**
   * Updates user's title and name.
   *
   * @return A Play result.
   */
  def editName(userID: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      EditNameForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => userService.retrieve(UUID.fromString(userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val updatedUser = user.copy(
              title = Some(data.title),
              firstName = Some(data.firstName),
              lastName = Some(data.lastName)
            )
            userService.save(updatedUser).map { usr =>
              Ok(ApiResponse(
                "account.details.updated",
                Messages("account.details.updated"),
                Json.toJson(usr)))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }.recover {
          case _: ProviderException =>
            BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
        }
      )
    }

  /**
   * Updates user's email.
   *
   * @return A Play result.
   */
  def editEmail(userID: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      EditEmailForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => {
          userService.retrieve(UUID.fromString(userID)).flatMap {
            case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
              authInfoRepository.find[PasswordInfo](user.loginInfo).flatMap { p =>
                if (!p.isEmpty && passwordHasher.matches(p.get, data.password)) {
                  val loginInfoUpdated = LoginInfo(CredentialsProvider.ID, data.email)
                  val passwordInfo = passwordHasherRegistry.current.hash(data.password)
                  for {
                    usr <- userService.save(user.copy(
                      loginInfo = loginInfoUpdated,
                      email = Some(data.email)
                    ))
                //    _ <- authInfoRepository.remove[PasswordInfo](usr.loginInfo)
                    _ <- authInfoRepository.add[PasswordInfo](loginInfoUpdated, passwordInfo)
                    authenticator <- env.authenticatorService.create(loginInfoUpdated)
                    token <- env.authenticatorService.init(authenticator)
                    result <- env.authenticatorService.embed(
                      token,
                      Ok(ApiResponse(
                        "account.details.updated",
                        Messages("account.details.updated"),
                        Json.toJson(usr)))
                    )
                  } yield {
                    env.eventBus.publish(SignUpEvent(usr, request))
                    env.eventBus.publish(LoginEvent(usr, request))
                    result
                  }
                } else {
                  Future.successful(
                    BadRequest(ApiResponse("account.invalid.password", Messages("account.invalid.password")))
                  )
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }.recover {
            case _: ProviderException =>
              BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
          }
        }
      )
    }

  /**
   * Updates user's password.
   *
   * @return A Play result.
   */
  def changePassword(userID: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      ChangePasswordForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => {
          userService.retrieve(UUID.fromString(userID)).flatMap {
            case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
              authInfoRepository.find[PasswordInfo](user.loginInfo).flatMap { p =>
                if (!p.isEmpty && passwordHasher.matches(p.get, data.oldPassword)) {
                  val passwordInfo = passwordHasherRegistry.current.hash(data.password)
                  for {
                    _ <- authInfoRepository.update[PasswordInfo](user.loginInfo, passwordInfo)
                    authenticator <- env.authenticatorService.create(user.loginInfo)
                    token <- env.authenticatorService.init(authenticator)
                    result <- env.authenticatorService.embed(
                      token,
                      Ok(ApiResponse(
                        "account.details.updated",
                        Messages("account.details.updated"),
                        Json.toJson(user)))
                    )
                  } yield {
                    env.eventBus.publish(SignUpEvent(user, request))
                    env.eventBus.publish(LoginEvent(user, request))
                    result
                  }
                } else {
                  Future.successful(
                    BadRequest(ApiResponse("account.details.invalid", Messages("account.details.invalid")))
                  )
                }
              }
            case None =>
              Future.failed(new IdentityNotFoundException("Couldn't find user."))
          }.recover {
            case _: ProviderException =>
              BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
          }
        }
      )
    }

  /**
   * Updates user's newsletter subscription.
   *
   * @return A Play result.
   */
  def updateNewsletter(userID: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      NewsletterForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(
          ApiResponse("account.details.invalid", Messages("account.details.invalid"), form.errors)
        )),
        data => userService.retrieve(UUID.fromString(userID)).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val updatedUser = user.copy(
              newsletters = Newsletter(
                newsletterFashion = NewsletterFashion(data.newsletterFashion),
                newsletterFineJewelry = NewsletterFineJewelry(data.newsletterFineJewelry),
                newsletterHomeCollection = NewsletterHomeCollection(data.newsletterHomeCollection)
              )
            )
            newsletterService.retrieve(user.email.get).flatMap {
              case Some(n) => newsletterService.save(n.copy(
                newsletterFashion = NewsletterFashion(data.newsletterFashion),
                newsletterFineJewelry = NewsletterFineJewelry(data.newsletterFineJewelry),
                newsletterHomeCollection = NewsletterHomeCollection(data.newsletterHomeCollection)
              ))
              case None =>
                val newsl = SubscribeToNewsletter(
                  id = UUID.randomUUID(),
                  email = user.email.get,
                  lang = request.lang,
                  newsletterFashion = NewsletterFashion(
                    isChecked = data.newsletterFashion
                  ),
                  newsletterFineJewelry = NewsletterFineJewelry(
                    isChecked = data.newsletterFineJewelry
                  ),
                  newsletterHomeCollection = NewsletterHomeCollection(
                    isChecked = data.newsletterHomeCollection
                  )
                )
                newsletterService.save(newsl)
                newsletterTaskService.create(
                  request.lang,
                  user.email.get,
                  data.newsletterFashion,
                  data.newsletterFineJewelry,
                  data.newsletterHomeCollection
                )
            }
            userService.save(updatedUser).map { usr =>
              val updatedUser = user.copy(
                newsletters = Newsletter(
                  newsletterFashion = NewsletterFashion(data.newsletterFashion),
                  newsletterFineJewelry = NewsletterFineJewelry(data.newsletterFineJewelry),
                  newsletterHomeCollection = NewsletterHomeCollection(data.newsletterHomeCollection)
                )
              )
              Ok(ApiResponse(
                "account.details.updated",
                Messages("account.details.updated"),
                Json.toJson(usr)))
            }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }.recover {
          case _: ProviderException =>
            BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
        }
      )
    }

}
