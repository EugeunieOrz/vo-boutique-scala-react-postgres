package models.newsletter.tables

import util.MyPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue

case class DbNewsletter(
  id: String,
  email: String,
  lang: JsValue,
  newsletterFashion: JsValue,
  newsletterFineJewelry: JsValue,
  newsletterHomeCollection: JsValue
)

trait SubscribeToNewsletterComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class SubscribeToNewsletterTable(tag: Tag) extends Table[DbNewsletter](tag, "newsletter") {

    def id: Rep[String] = column[String]("id", O.PrimaryKey)
    def email: Rep[String] = column[String]("email")
    def lang: Rep[JsValue] = column[JsValue]("lang")
    def newsletterFashion: Rep[JsValue] = column[JsValue]("newsletter_fashion")
    def newsletterFineJewelry: Rep[JsValue] = column[JsValue]("newsletter_fine_jewelry")
    def newsletterHomeCollection: Rep[JsValue] = column[JsValue]("newsletter_home_collection")
    def * = (
      id, email, lang,
      newsletterFashion,
      newsletterFineJewelry,
      newsletterHomeCollection
    ) <> (DbNewsletter.tupled, DbNewsletter.unapply)

  }

}
