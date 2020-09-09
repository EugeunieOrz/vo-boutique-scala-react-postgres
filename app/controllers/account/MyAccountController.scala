package controllers.account

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import java.util.UUID
import javax.inject.Inject

import com.google.inject.name.Named

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import models.core.User
import models.core.services.UserService
import models.newsletter.services.{ NewsletterService, NewsletterTaskService }
import utils.core.JSRouter
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
class MyAccountController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
  @Named("customer-auth-info-repository") authInfoRepository: AuthInfoRepository,
  userService: UserService,
  newsletterService: NewsletterService,
  newsletterTaskService: NewsletterTaskService
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  /**
   * Removes user's account.
   *
   * @return A Play result.
   */
  def removeAccount(userID: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      userService.retrieve(UUID.fromString(userID)).flatMap {
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          val loginInfo = LoginInfo(CredentialsProvider.ID, user.email.get)
          user.email.map { email =>
            newsletterTaskService.retrieve(email).flatMap {
              case Some(task) => newsletterTaskService.delete(task.id)
              case None => Future.successful(println(s"newsletter task for the customer ${user.email.get} not found"))
            }
            newsletterService.retrieve(email).flatMap {
              case Some(newsletter) => newsletterService.deleteSubscription(newsletter.id)
              case None => Future.successful(println(s"newsletter task for the customer ${user.email.get} not found"))
            }
          }
          for {
            _ <- authInfoRepository.remove[PasswordInfo](loginInfo)
            _ <- userService.deleteAccount(user.userID)
          } yield {
            env.eventBus.publish(LogoutEvent(request.identity, request))
            env.authenticatorService.discard(request.authenticator, Ok(ApiResponse(
              "auth.signed.out",
              Messages("auth.signed.out")
            )))
            Ok(ApiResponse(
              "account.deleted.successfully",
              Messages("account.deleted.successfully")
            ))
          }
        case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }.recover {
        case _: ProviderException =>
          BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
      }
    }

}
