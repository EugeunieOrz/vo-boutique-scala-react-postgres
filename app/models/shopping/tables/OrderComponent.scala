package models.shopping.tables

import java.util.UUID
import util.MyPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue

case class DbOrder(
  id: UUID,
  content: JsValue
)

trait OrderComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class OrderTable(tag: Tag) extends Table[DbOrder](tag, "orders") {

    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def content: Rep[JsValue] = column[JsValue]("content")
    def * = (id, content) <> (DbOrder.tupled, DbOrder.unapply)

  }


}
