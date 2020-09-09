package controllers.auth

import com.mohiva.play.silhouette.api.Silhouette
import models.core.User
import utils.silhouette.{ BoutiqueEnv, CustomerController }
import utils.core.JSRouter
import utils.formats.Formats._
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

/**
 * The `User` controller.
 *
 * @param controllerComponents  The Play controller components.
 * @param silhouette            The Silhouette stack.
 */
class UserController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
) extends CustomerController {

  /**
   * Gets a user.
   *
   * @return A Play result.
   */
  def get: Action[AnyContent] = SecuredAction { implicit request =>
    Ok(ApiResponse(
      "request.ok",
      Messages("request.ok"),
      Json.toJson(request.identity)
    ))
  }
}
