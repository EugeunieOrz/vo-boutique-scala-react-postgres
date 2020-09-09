package models.shopping.tables

import java.util.UUID
import util.MyPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue

case class DbTransaction(
  id: UUID,
  content: JsValue
)

trait TransactionComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class TransactionTable(tag: Tag) extends Table[DbTransaction](tag, "transactions") {

    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def content: Rep[JsValue] = column[JsValue]("content")
    def * = (id, content) <> (DbTransaction.tupled, DbTransaction.unapply)

  }


}
