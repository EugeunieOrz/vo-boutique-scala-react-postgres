package models.newsletter.tables

import util.MyPostgresDriver
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.json.JsValue

case class DbNewsletterTask(
  id: String,
  email: String,
  lang: JsValue,
  expiry: JsValue,
  newsletterFashion: Boolean,
  newsletterFineJewelry: Boolean,
  newsletterHomeCollection: Boolean
)

trait NewsletterTaskComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class NewsletterTaskTable(tag: Tag) extends Table[DbNewsletterTask](tag, "newsletter_task") {

    def id: Rep[String] = column[String]("id", O.PrimaryKey)
    def email: Rep[String] = column[String]("email")
    def lang: Rep[JsValue] = column[JsValue]("lang")
    def expiry: Rep[JsValue] = column[JsValue]("expiry")
    def newsletterFashion: Rep[Boolean] = column[Boolean]("newsletter_fashion")
    def newsletterFineJewelry: Rep[Boolean] = column[Boolean]("newsletter_fine_jewelry")
    def newsletterHomeCollection: Rep[Boolean] = column[Boolean]("newsletter_home_collection")
    def * = (
      id, email, lang, expiry,
      newsletterFashion,
      newsletterFineJewelry,
      newsletterHomeCollection
    ) <> (DbNewsletterTask.tupled, DbNewsletterTask.unapply)

  }

}
