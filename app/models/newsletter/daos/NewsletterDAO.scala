package models.newsletter.daos

/**
 * Author: Ievgeniia Ozirna
 *
 * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
import java.util.UUID
import javax.inject.{ Inject, Singleton }

import scala.concurrent.Future

import models.core.{
  NewsletterFashion,
  NewsletterFineJewelry,
  NewsletterHomeCollection,
  SubscribeToNewsletter
}
import models.newsletter.tables._
import util.MyPostgresDriver
import utils.formats.Formats._
import utils.core.CustomerExecutionContext

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.i18n.Lang
import play.api.libs.json._

/**
 * Give access to the [[SubscribeToNewsletter]] object.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param ec               The execution context.
 */
 @Singleton
 class NewsletterDAO @Inject()
 (protected val dbConfigProvider: DatabaseConfigProvider)(
   implicit ec: CustomerExecutionContext
 ) extends SubscribeToNewsletterComponent
 with HasDatabaseConfigProvider[MyPostgresDriver] {

   import profile.api._

   private val newsletters = TableQuery[SubscribeToNewsletterTable]

  /**
   * Finds a newsletter by its email.
   *
   * @param email The email of the newsletter to find.
   * @return The found newsletter or None if no newsletter for the given email could be found.
   */
   def find(email: String): Future[Option[SubscribeToNewsletter]] = {

     db.run(newsletters.filter(_.email === email).result.headOption).map { resultOption =>
       resultOption.map {
         newsletter =>
         SubscribeToNewsletter(
           UUID.fromString(newsletter.id),
           newsletter.email,
           newsletter.lang.validate[Lang] match {
             case JsSuccess(value, _) => value
             case e: JsError => Lang("en")
           },
           newsletter.newsletterFashion.validate[NewsletterFashion] match {
             case JsSuccess(value, _) => NewsletterFashion(value.isChecked)
             case e: JsError =>
             println(s"JsError: $e")
             NewsletterFashion(false)
           },
           newsletter.newsletterFineJewelry.validate[NewsletterFineJewelry] match {
             case JsSuccess(value, _) => NewsletterFineJewelry(value.isChecked)
             case e: JsError =>
             println(s"JsError: $e")
             NewsletterFineJewelry(false)
           },
           newsletter.newsletterHomeCollection.validate[NewsletterHomeCollection] match {
             case JsSuccess(value, _) => NewsletterHomeCollection(value.isChecked)
             case e: JsError =>
             println(s"JsError: $e")
             NewsletterHomeCollection(false)
           }
         )
       }
     }
   }

  /**
   * Finds a newsletter by its user ID.
   *
   * @param userID The ID of the newsletter to find.
   * @return The found newsletter or None if no newsletter for the given ID could be found.
   */
   def find(id: UUID): Future[Option[SubscribeToNewsletter]] = {

     db.run(newsletters.filter(_.id === id.toString).result.headOption).map { resultOption =>
       resultOption.map {
         newsletter => SubscribeToNewsletter(
           UUID.fromString(newsletter.id),
           newsletter.email,
           newsletter.lang.validate[Lang] match {
             case JsSuccess(value, _) => value
             case e: JsError => Lang("en")
           },
           newsletter.newsletterFashion.validate[NewsletterFashion] match {
             case JsSuccess(value, _) => NewsletterFashion(
               value.isChecked,
               value.description
             )
             case e: JsError => NewsletterFashion(
               false,
               "I would like to receive a Fashion newsletter from Vittoria Orzini Fashion Boutique."
             )
           },
           newsletter.newsletterFineJewelry.validate[NewsletterFineJewelry] match {
             case JsSuccess(value, _) => NewsletterFineJewelry(
               value.isChecked,
               value.description
             )
             case e: JsError => NewsletterFineJewelry(
               false,
               "I would like to receive a Fine Jewelry newsletter from Vittoria Orzini Fashion Boutique."
             )
           },
           newsletter.newsletterHomeCollection.validate[NewsletterHomeCollection] match {
             case JsSuccess(value, _) => NewsletterHomeCollection(
               value.isChecked,
               value.description
             )
             case e: JsError => NewsletterHomeCollection(
               false,
               "I would like to receive a Home Collection newsletter from Vittoria Orzini Fashion Boutique."
             )
           }
         )
       }
     }
   }

  /**
   * Saves a newsletter.
   *
   * @param user The newsletter to save.
   * @return The saved newsletter.
   */
   def save(newsletter: SubscribeToNewsletter): Future[SubscribeToNewsletter] = {
     val dbNewsletter = DbNewsletter(
       newsletter.id.toString,
       newsletter.email,
       Json.toJson(newsletter.lang),
       Json.toJson(newsletter.newsletterFashion),
       Json.toJson(newsletter.newsletterFineJewelry),
       Json.toJson(newsletter.newsletterHomeCollection)
     )
     db.run(newsletters.insertOrUpdate(dbNewsletter)).map(_ => newsletter)
   }

  /**
   * Deletes a newsletter subscription.
   *
   * @param id The id of the newsletter.
   * @return A future to wait for the process to be completed.
   */
  def removeSubscription(id: UUID): Future[Int] = {
    db.run(newsletters.filter(_.id === id.toString).delete)
  }
}
