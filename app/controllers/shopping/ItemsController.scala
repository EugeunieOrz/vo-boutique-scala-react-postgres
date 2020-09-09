package controllers.shopping

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

import models.core.Item._
import models.core.{ Item, User }
import models.shopping.services.ItemService
import utils.core.JSRouter
import utils.core.SomeMethods._
import utils.formats.Formats._
import utils.silhouette.{ BoutiqueEnv, CustomerController }
import javax.inject.Inject
import net.ceedubs.ficus.Ficus._
import com.mohiva.play.silhouette.api._
import org.joda.time.DateTime
import play.api.i18n.{ Messages, MessagesProvider }
import play.api.libs.json._
import play.api.mvc._
import com.mohiva.play.silhouette.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Shopping` controller.
 *
 * @param controllerComponents    The Play controller components.
 * @param itemService             The item service implementation.
 * @param ex                      The execution context.
 */
class ItemsController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
  itemService: ItemService,
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  /**
   * Creates item collections if they don't exist, saves them in the database,
   * passes them to the front-end.
   *
   * @return The items.
   */
  def getProducts: Action[AnyContent] = Action.async { implicit request =>

    val dressesCollection = Item.createDressesCollection
    val eveningCollection = Item.createEveningCollection
    val jacketsCollection = Item.createJacketsCollection

    dressesCollection.map { dress =>
      itemService.save(dress)
    }
    eveningCollection.map { evening =>
      itemService.save(evening)
    }
    jacketsCollection.map { jacket =>
      itemService.save(jacket)
    }

    val collection = dressesCollection ++ eveningCollection ++ jacketsCollection

    Future.successful(
      Ok(ApiResponse("request.ok", Messages("request.ok"), Json.toJson(collection)))
    )
  }

  /**
   * Gets the items where inventory is more than zero,
   * sets the status of item "Last Item" if its quantity equals 1
   * and "Not Available" - if its quantity equals 0.
   * Then passes the available items to the React component.
   *
   * @return The items.
   */
  def get: Action[AnyContent] = Action.async { implicit request =>

    val items = itemService.retrieveAll.map { products =>
      val checkAvailability = products.collect {
        case product if (product.inventory > 1) => product.copy(
          size = product.size.collect {
            case s if (s.quantity > 1) => s
            case s if (s.quantity == 1) => s.copy(
              availability = "Last Item"
            )
            case s if (s.quantity == 0) => s.copy(
              availability = "Not Available"
            )
          }
        )
        case product if (product.inventory == 1) => product.copy(
          size = product.size.collect {
            case s if (s.quantity > 1) => s
            case s if (s.quantity == 1) => s.copy(
              availability = "Last Item"
            )
            case s if (s.quantity == 0) => s.copy(
              availability = "Not Available"
            )
          },
          availability = "Last Item"
        )
        case product if (product.inventory == 0) => product.copy(
          size = product.size.map { s =>
            s.copy(
              availability = "Not Available"
            )
          },
          availability = "Not Available"
        )
      }
      checkAvailability.map { product => itemService.save(product) }
      checkAvailability.filter(p =>
        p.inventory > 0 && p.availability != "Not Available"
      )
    }

    for {
      products <- items
    } yield {
      Ok(ApiResponse("request.ok", Messages("request.ok"), Json.toJson(products)))
    }
  }

}
