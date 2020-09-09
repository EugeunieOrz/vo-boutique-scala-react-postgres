package models.shopping.tables

import java.util.UUID
import util.MyPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue

trait ItemComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class ItemTable(tag: Tag) extends Table[DbItem](tag, "item") {

    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String] = column[String]("name")
    def description: Rep[String] = column[String]("description")
    def details: Rep[List[String]] = column[List[String]]("details")
    def composition: Rep[JsValue] = column[JsValue]("composition")
    def color: Rep[JsValue] = column[JsValue]("color")
    def size: Rep[JsValue] = column[JsValue]("size")
    def inventory: Rep[Int] = column[Int]("inventory")
    def price: Rep[Float] = column[Float]("price")
    def currency: Rep[String] = column[String]("currency")
    def nameOfImg: Rep[Int] = column[Int]("name_of_img")
    def category: Rep[String] = column[String]("category")
    def subCategory: Rep[String] = column[String]("sub_category")
    def stateOfProduct: Rep[String] = column[String]("state_of_product")
    def department: Rep[String] = column[String]("department")
    def typeOfCollection: Rep[String] = column[String]("type_of_collection")
    def links: Rep[List[String]] = column[List[String]]("links")
    def availability: Rep[String] = column[String]("availability")
    def shippingCosts: Rep[Float] = column[Float]("shipping_costs")
    def total: Rep[Float] = column[Float]("total")
    def * = (
      id, name, description, details, composition, color, size,
      inventory, price, currency, nameOfImg, category, subCategory,
      stateOfProduct, department, typeOfCollection, links,
      availability, shippingCosts, total
    ) <> (DbItem.tupled, DbItem.unapply)

  }


}
