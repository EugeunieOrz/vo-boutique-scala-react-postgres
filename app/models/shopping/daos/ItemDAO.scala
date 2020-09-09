package models.shopping.daos


import java.util.UUID
import javax.inject.{ Inject, Singleton }

import models.core.{Color, Composition, Item, ItemComposition, ItemSize, Size}
import models.shopping.tables._
import util.MyPostgresDriver
import utils.core.CustomerExecutionContext
import utils.formats.Formats._

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.json._

import scala.concurrent.Future

/**
 * Give access to the [[Item]] object.
 *
 * @param ec               The execution context.
 */
 @Singleton
 class ItemDAO @Inject() (
   protected val dbConfigProvider: DatabaseConfigProvider
 )(implicit ec: CustomerExecutionContext) extends ItemComponent with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val items = TableQuery[ItemTable]

  /**
   * Finds a item by its id.
   *
   * @param id The ID of the item to find.
   * @param category The category of the item to find.
   * @return The found item or None if no item for the given ID could be found.
   */
  def find(id: UUID): Future[Option[Item]] = {
    val itemQuery = for {
      dbItem <- items.filter(i => i.id === id)
    } yield dbItem

    db.run(itemQuery.result.headOption).map { dbItemOption =>
      dbItemOption.map {
        case item => Item(
          item.id,
          item.name,
          item.description,
          item.details,
          item.composition.validate[ItemComposition] match {
            case JsSuccess(value, _) => value.content
            case e: JsError => Seq(Composition(
              fabric = "Cotton",
              percentage = "100%"
            ))
          },
          item.color.validate[Color] match {
            case JsSuccess(value, _) => value
            case e: JsError => Color(
              color = "Brown",
              imgIndex = "brown"
            )
          },
          item.size.validate[ItemSize] match {
            case JsSuccess(value, _) => value.content
            case e: JsError => Seq(
              Size(
                number = "XS",
                quantity = 4
              ),
              Size(
                number = "S",
                quantity = 3
              ),
              Size(
                number = "M",
                quantity = 4
              ),
              Size(
                number = "L",
                quantity = 2
              )
            )
          },
          item.inventory,
          item.price.toDouble,
          item.currency,
          item.nameOfImg,
          item.category,
          item.subCategory,
          item.stateOfProduct,
          item.department,
          item.typeOfCollection,
          item.links,
          item.availability,
          item.shippingCosts.toDouble,
          item.total.toDouble
        )
      }
    }
  }

  /**
   * Retrieves all the items in a collection by category.
   * @param category The category of the items.
   */
  def findAll: Future[Seq[Item]] = {
    db.run(items.result).map { dbItemOption =>
      dbItemOption.map {
        case item => Item(
          item.id,
          item.name,
          item.description,
          item.details,
          item.composition.validate[ItemComposition] match {
            case JsSuccess(value, _) => value.content
            case e: JsError => Seq(Composition(
              fabric = "Cotton",
              percentage = "100%"
            ))
          },
          item.color.validate[Color] match {
            case JsSuccess(value, _) => value
            case e: JsError => Color(
              color = "Brown",
              imgIndex = "brown"
            )
          },
          item.size.validate[ItemSize] match {
            case JsSuccess(value, _) => value.content
            case e: JsError => Seq(
              Size(
                number = "XS",
                quantity = 4
              ),
              Size(
                number = "S",
                quantity = 3
              ),
              Size(
                number = "M",
                quantity = 4
              ),
              Size(
                number = "L",
                quantity = 2
              )
            )
          },
          item.inventory,
          item.price.toDouble,
          item.currency,
          item.nameOfImg,
          item.category,
          item.subCategory,
          item.stateOfProduct,
          item.department,
          item.typeOfCollection,
          item.links,
          item.availability,
          item.shippingCosts.toDouble,
          item.total.toDouble
        )
      }
    }
  }

  /**
   * Saves a item.
   *
   * @param item The item item to save.
   * @return The saved item.
   */
  def save(item: Item): Future[Item] = {
    val dbItem = DbItem(
      item.id,
      item.name,
      item.description,
      item.details.toList,
      Json.toJson(ItemComposition("Composition", item.composition)),
      Json.toJson(item.color),
      Json.toJson(ItemSize("Size", item.size)),
      item.inventory,
      item.price.toFloat,
      item.currency,
      item.nameOfImg,
      item.category,
      item.subCategory,
      item.stateOfProduct,
      item.department,
      item.typeOfCollection,
      item.links.toList,
      item.availability,
      item.shippingCosts.toFloat,
      item.total.toFloat
    )
    db.run(items.insertOrUpdate(dbItem)).map(_ => item)
  }

}
