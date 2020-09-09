package controllers.auth

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import java.time.Clock
import java.util.UUID

import forms.auth.{ EmailForm, SignInForm }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.google.inject.name.Named
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import utils.silhouette.{ BoutiqueEnv, CustomerController }
import utils.formats.Formats._
import models.core.{
  User,
  LoginAttempt,
  UserLoginAttempts,
  CompleteSignIn,
  SignInToShop
}
import models.core.services.UserService
import javax.inject.Inject
import net.ceedubs.ficus.Ficus._
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.Configuration
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.json._
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign In` controller.
 *
 * @param controllerComponents  The Play controller components.
 * @param silhouette            The Silhouette stack.
 * @param userService           The user service implementation.
 * @param creditCardsService     The credit cards service implementation.
 * @param newsletterService      The newsletter subscription service implementation.
 * @param addressesService       The addresses service implementation.
 * @param credentialsProvider   The credentials provider.
 * @param configuration         The Play configuration.
 * @param clock                 The clock instance.
 * @param ex                    The execution context.
 */
class SignInController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
  @Named("customer-credentials-provider") credentialsProvider: CredentialsProvider,
  mailerClient: MailerClient,
  messagesApi: MessagesApi,
  userService: UserService,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  /**
   * Sign in a user.
   *
   * @return A Play result.
   */
  def signIn: Action[AnyContent] = UnsecuredAction.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(
        ApiResponse("invalid.form", Messages("invalid.form"), form.errors)
      )),
      data => {
        credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              if (user.accountStatus != "Locked") {
                handleActiveUser(
                  user,
                  loginInfo,
                  data.rememberMe
                )
              } else {
                Future.successful(
                  BadRequest(ApiResponse(
                    "auth.email.account.locked",
                    Messages("auth.email.account.locked")
                  ))
                )
              }
            case None =>
              Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            countLoginAttempts(data.email)
            BadRequest(ApiResponse("auth.invalid.credentials", Messages("auth.invalid.credentials")))
        }
      }
    )
  }

  /**
   * Sign in a user with a shopping bag.
   *
   * @return A Play result.
   */
  def signInToShop: Action[JsValue] = UnsecuredAction(parse.json).async { implicit request =>
    request.body.validate[SignInToShop].fold(
      errors => Future.successful(
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
      data => {
        credentialsProvider.authenticate(Credentials(data.data.email, data.data.password)).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              user.shoppingBag match {
                case Some(shoppingB) =>
                  if (user.accountStatus != "Locked") {
                    val mergedItems = shoppingB.addedItems ++ data.shoppingBag.addedItems
                    val updatedUser = user.copy(
                      shoppingBag = Some(shoppingB.copy(
                        addedItems = mergedItems,
                        total = shoppingB.total + data.shoppingBag.total
                      ))
                    )
                    userService.save(updatedUser)
                    handleActiveUser(
                      updatedUser,
                      loginInfo,
                      data.data.rememberMe
                    )
                  } else {
                    Future.successful(
                      BadRequest(ApiResponse(
                        "auth.email.account.locked",
                        Messages("auth.email.account.locked")
                      ))
                    )
                  }
                case None =>
                  if (user.accountStatus != "Locked") {
                    val updatedUser = user.copy(
                      shoppingBag = Some(data.shoppingBag)
                    )
                    userService.save(updatedUser)
                    handleActiveUser(
                      updatedUser,
                      loginInfo,
                      data.data.rememberMe
                    )
                  } else {
                    Future.successful(
                      BadRequest(ApiResponse(
                        "auth.email.account.locked",
                        Messages("auth.email.account.locked")
                      ))
                    )
                  }
              }
            case None =>
              Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            countLoginAttempts(data.data.email)
            BadRequest(ApiResponse("auth.invalid.credentials", Messages("auth.invalid.credentials")))

        }
      }
    )
  }

  def completeSignIn: Action[JsValue] = UnsecuredAction(parse.json).async { implicit request =>
    request.body.validate[CompleteSignIn].fold(
      errors => Future.successful(
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
      data => {
        credentialsProvider.authenticate(Credentials(data.email, data.data.password)).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              user.shoppingBag match {
                case Some(shoppingB) =>
                  if (user.accountStatus != "Locked") {
                    val mergedItems = shoppingB.addedItems ++ data.shoppingBag.addedItems
                    val updatedUser = user.copy(
                      shoppingBag = Some(shoppingB.copy(
                        addedItems = mergedItems,
                        total = shoppingB.total + data.shoppingBag.total
                      ))
                    )
                    userService.save(updatedUser)
                    handleActiveUser(
                      updatedUser,
                      loginInfo,
                      true
                    )
                  } else {
                    Future.successful(
                      BadRequest(ApiResponse(
                        "auth.email.account.locked",
                        Messages("auth.email.account.locked")
                      ))
                    )
                  }
                case None =>
                  if (user.accountStatus != "Locked") {
                    val updatedUser = user.copy(
                      shoppingBag = Some(data.shoppingBag)
                    )
                    userService.save(updatedUser)
                    handleActiveUser(
                      updatedUser,
                      loginInfo,
                      true
                    )
                  } else {
                    Future.successful(
                      BadRequest(ApiResponse(
                        "auth.email.account.locked",
                        Messages("auth.email.account.locked")
                      ))
                    )
                  }
              }
            case None =>
              Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            countLoginAttempts(data.email)
            BadRequest(ApiResponse("auth.invalid.credentials", Messages("auth.invalid.credentials")))

        }
      }
    )
  }

  /**
   * Handles the active user.
   *
   * @param user       The active user.
   * @param addresses  The addresses of the active user.
   * @param newsletter The newsletter subscription data of the active user.
   * @param creditCards The credit cards of the active user.
   * @param loginInfo  The login info for the current authentication.
   * @param rememberMe True if the cookie should be a persistent cookie, false otherwise.
   * @param request    The current request header.
   * @return A Play result.
   */
  private def handleActiveUser(
    user: User,
    loginInfo: LoginInfo,
    rememberMe: Boolean
  )(implicit request: RequestHeader): Future[Result] = {
    for {
      authenticator <- env.authenticatorService.create(loginInfo)
      cookie <- env.authenticatorService.init(configureAuthenticator(rememberMe, authenticator))
      result <- env.authenticatorService.embed(
        cookie,
        Ok(ApiResponse(
          "auth.signed.in",
          Messages("auth.signed.in"),
          Json.toJson((user))
        ))
      )
    } yield {
      env.eventBus.publish(LoginEvent(user, request))
      result
    }
  }

  /**
   * Changes the default authenticator config if the remember me flag was activated during sign-in.
   *
   * @param rememberMe    True if the cookie should be a persistent cookie, false otherwise.
   * @param authenticator The authenticator instance.
   * @return The changed authenticator if the remember me flag was activated, otherwise the unchanged authenticator.
   */
  private def configureAuthenticator(rememberMe: Boolean, authenticator: CookieAuthenticator): CookieAuthenticator = {
    if (rememberMe) {
      val c = configuration.underlying
      val configPath = "silhouette.authenticator.rememberMe"
      val authenticatorExpiry = c.as[FiniteDuration](s"$configPath.authenticatorExpiry").toMillis
      val instant = clock.instant().plusMillis(authenticatorExpiry)
      val expirationDateTime = new DateTime(instant.toEpochMilli)

      authenticator.copy(
        expirationDateTime = expirationDateTime,
        idleTimeout = c.getAs[FiniteDuration](s"$configPath.authenticatorIdleTimeout"),
        cookieMaxAge = c.getAs[FiniteDuration](s"$configPath.cookieMaxAge")
      )
    } else {
      authenticator
    }
  }

  private def countLoginAttempts(email: String)(
    implicit
    request: RequestHeader
  ): Future[Result] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) =>
        user.loginAttempts match {
          case Some(attempts) =>
            val lang = user.registration.lang
            val dateNow = DateTime.now().withZone(DateTimeZone.UTC).toLocalDate()
            // Check if login failed today
            if (attempts.exists(a =>
                a.dateTime.withZone(DateTimeZone.UTC).toLocalDate()
                 .isEqual(dateNow)
            )) {
              val loginAtt = attempts.find(a =>
                a.dateTime.withZone(DateTimeZone.UTC).toLocalDate()
                 .isEqual(dateNow)).get

              if (loginAtt.loginAttempts.exists(a => a.count < 3)) {
                if(!loginAtt.emailLoginAttemptSent) {
                  mailerClient.send(Email(
                    subject = messagesApi("auth.email.login.attempts.subject")(lang),
                    from = messagesApi("email.from")(lang),
                    to = Seq(email),
                    bodyText = Some(twirl.core.views.txt.emails.loginAttempts(
                      user, email
                    )(messagesApi, lang).body),
                    bodyHtml = Some(twirl.core.views.html.emails.loginAttempts(
                      user, email
                    )(messagesApi, lang).body)
                  ))
                }
                val newSeq = attempts.collect {
                  case att if(att.dateTime.withZone(DateTimeZone.UTC).toLocalDate()
                   .isEqual(dateNow)) => att.copy(
                     emailLoginAttemptSent = true,
                     loginAttempts = att.loginAttempts.collect {
                       case l if(l.count < 3) => l.copy(count = l.count + 1)
                       case l if(l.count >= 3) => l.copy(count = l.count + 1)
                     }
                   )
                  case att if(!att.dateTime.withZone(DateTimeZone.UTC).toLocalDate()
                   .isEqual(dateNow)) => att
                }
                userService.save(user.copy(
                  loginAttempts = Some(newSeq)
                )).map { _ =>
                  BadRequest(ApiResponse(
                    "auth.email.login.attempt.failed",
                    Messages("auth.email.login.attempt.failed")
                  ))
                }
              } else {
                if(!loginAtt.emailAccountLockedSent) {
                  mailerClient.send(Email(
                    subject = messagesApi("auth.email.login.attempts.subject")(lang),
                    from = messagesApi("email.from")(lang),
                    to = Seq(email),
                    bodyText = Some(twirl.core.views.txt.emails.accountLocked(
                      user, email
                    )(messagesApi, lang).body),
                    bodyHtml = Some(twirl.core.views.html.emails.accountLocked(
                      user, email
                    )(messagesApi, lang).body)
                  ))
                }
                val newSeq = attempts.collect {
                  case att if(att.dateTime.withZone(DateTimeZone.UTC).toLocalDate()
                   .isEqual(dateNow)) => att.copy(
                     emailAccountLockedSent = true,
                     loginAttempts = att.loginAttempts.collect {
                       case l if(l.count < 3) => l.copy(count = l.count + 1)
                       case l if(l.count >= 3) => l.copy(count = l.count + 1)
                     }
                   )
                  case att if(!att.dateTime.withZone(DateTimeZone.UTC).toLocalDate()
                   .isEqual(dateNow)) => att
                }
                userService.save(user.copy(
                  accountStatus = Some("Locked"),
                  loginAttempts = Some(newSeq)
                )).map { _ =>
                  BadRequest(ApiResponse(
                    "auth.email.account.locked",
                    Messages("auth.email.account.locked")
                  ))
                }
              }
            } else {
              val att = LoginAttempt(
                dateTime = DateTime.now(),
                count = 1
              )
              val userLoginAtt = UserLoginAttempts(
                id = UUID.randomUUID(),
                dateTime = DateTime.now(),
                emailLoginAttemptSent = false,
                emailAccountLockedSent = false,
                loginAttempts = Seq(att)
              )
              val newSeqOfAttempts = attempts :+ userLoginAtt

              userService.save(user.copy(
                loginAttempts = Some(newSeqOfAttempts)
              )).map { _ =>
                BadRequest(ApiResponse(
                  "auth.email.login.attempt.failed",
                  Messages("auth.email.login.attempt.failed")
                ))
              }
            }

          case None =>
            userService.save(user.copy(
              loginAttempts = Some(Seq(UserLoginAttempts(
                id = UUID.randomUUID(),
                dateTime = DateTime.now(),
                emailLoginAttemptSent = false,
                emailAccountLockedSent = false,
                loginAttempts = Seq(LoginAttempt(
                  dateTime = DateTime.now(),
                  count = 1
                ))
              )))
            )).map { user =>
              BadRequest(ApiResponse(
                "auth.email.login.attempt.failed",
                Messages("auth.email.login.attempt.failed")
              ))
            }
        }

      case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))

    }
  }

  def checkEmail: Action[AnyContent] = UnsecuredAction.async { implicit request =>
    EmailForm.form.bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(
          ApiResponse("invalid.form", Messages("invalid.form"), form.errors)
        ))
      },
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
          Future.successful(Ok(ApiResponse(
            "auth.email.already.in.use",
            Messages("auth.email.already.in.use"),
            Json.toJson(data.email)
          )))
          case None => Future.successful(Ok(ApiResponse(
            "auth.email.not.found",
            Messages("auth.email.not.found")
          )))
        }
      }
    )
  }

}
