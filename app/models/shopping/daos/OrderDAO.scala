package models.shopping.daos

import java.time.Instant
import java.util.UUID
import javax.inject.{ Inject, Singleton }

import models.core.{CountryAtPurchase, Order, ShoppingBag}
import models.shopping.tables._
import util.MyPostgresDriver
import utils.core.CustomerExecutionContext
import utils.formats.Formats._

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.json._

import scala.concurrent.Future

/**
 * Give access to the [[Order]] object.
 *
 * @param ec               The execution context.
 */
 @Singleton
 class OrderDAO @Inject() (
   protected val dbConfigProvider: DatabaseConfigProvider
 )(implicit ec: CustomerExecutionContext) extends OrderComponent with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val orders = TableQuery[OrderTable]

  private def createOrder: Order = Order(
    UUID.randomUUID(),
    None,
    None,
    None,
    "Order Status"
  )

  /**
   * Finds a order by its id.
   *
   * @param id The ID of the order to find.
   * @return The found order or None if no customer for the given ID could be found.
   */
  def find(id: UUID): Future[Option[Order]] = {
    val orderQuery = for {
      dbOrder <- orders.filter(_.id === id)
    } yield dbOrder

    db.run(orderQuery.result.headOption).map { dbOrderOption =>
      dbOrderOption.map {
        case order => order.content.validate[Order] match {
          case JsSuccess(value, _) => value
          case e: JsError => createOrder
        }
      }
    }
  }

  /**
   * Retrieves all the orders in a collection.
   */
  def findAll: Future[Seq[Order]] = {
    val orderSeq = Seq[Order]()
    db.run(orders.result.headOption).map { dbOrderOption =>
      dbOrderOption.map {
        case order => order.content.validate[Order] match {
          case JsSuccess(value, _) => value +: orderSeq
          case e: JsError => createOrder
        }
      }
    }
    Future.successful(orderSeq)
  }

  /**
   * Saves a order.
   *
   * @param user The order to save.
   * @return The saved order.
   */
  def save(order: Order): Future[Order] = {
    val dbOrder = DbOrder(
      order.id,
      Json.toJson(order),
    )
    db.run(orders.insertOrUpdate(dbOrder)).map(_ => order)
  }

  /**
   * Deletes the order for the given ID.
   *
   * @param id The ID for which the order should be removed.
   * @return A future to wait for the process to be completed.
   */
  def delete(id: UUID): Future[Int] =
    db.run(orders.filter(_.id === id).delete)

}
