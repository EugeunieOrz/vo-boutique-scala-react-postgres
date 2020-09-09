package models.newsletter.daos

import java.time.{ Clock, Instant }
import java.util.UUID
import javax.inject.{ Inject, Singleton }

import models.newsletter.NewsletterTask
import models.newsletter.tables._
import util.MyPostgresDriver
import utils.formats.Formats._
import utils.core.CustomerExecutionContext

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.i18n.Lang
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Give access to the [[NewsletterTask]] object.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param ec               The execution context.
 */
@Singleton
class NewsletterTaskDAO @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider,
  clock: Clock
)(implicit ec: CustomerExecutionContext) extends NewsletterTaskComponent
with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val tasks = TableQuery[NewsletterTaskTable]

  /**
   * Finds a newsletter task by its ID.
   *
   * @param id The unique newsletter task ID.
   * @return The found newsletter task or None if no newsletter task for the given ID could be found.
   */
   def find(id: UUID): Future[Option[NewsletterTask]] = {
     db.run(tasks.filter(_.id === id.toString).result.headOption).map { resultOption =>
       resultOption.map {
         dbTask => NewsletterTask(
           UUID.fromString(dbTask.id),
           dbTask.email,
           dbTask.lang.validate[Lang] match {
             case JsSuccess(value, _) => value
             case e: JsError => Lang("en")
           },
           dbTask.expiry.validate[Instant] match {
             case JsSuccess(value, _) => value
             case e: JsError => clock.instant()
           },
           dbTask.newsletterFashion,
           dbTask.newsletterFineJewelry,
           dbTask.newsletterHomeCollection
         )
       }
     }
   }

  /**
   * Finds a newsletter task by its email.
   *
   * @param email The unique newsletter task email.
   * @return The found newsletter task or None if no newsletter task for the given email could be found.
   */
   def find(email: String): Future[Option[NewsletterTask]] = {
     db.run(tasks.filter(_.email === email).result.headOption).map { resultOption =>
       resultOption.map {
         dbTask => NewsletterTask(
           UUID.fromString(dbTask.id),
           dbTask.email,
           dbTask.lang.validate[Lang] match {
             case JsSuccess(value, _) => value
             case e: JsError => Lang("en")
           },
           dbTask.expiry.validate[Instant] match {
             case JsSuccess(value, _) => value
             case e: JsError => clock.instant()
           },
           dbTask.newsletterFashion,
           dbTask.newsletterFineJewelry,
           dbTask.newsletterHomeCollection
         )
       }
     }
   }

  /**
   * Finds expired newsletter tasks.
   *
   * @param instant The current instant.
   */
   def findExpired(instant: Instant): Future[Seq[NewsletterTask]] = {
     val expiry: FiniteDuration = 7 days

     val taskSeq = Seq[NewsletterTask]()
     val someTasks = Seq[NewsletterTask]()
     val taskQuery = for {
       task <- tasks
     } yield task

     db.run(taskQuery.result).map { taskOption =>
       taskOption.flatMap {
         case dbTask =>
           val parsed = dbTask.expiry.validate[Instant] match {
             case JsSuccess(value, _) => value
             case e: JsError => clock.instant().plusSeconds(expiry.toSeconds)
           }
         if(parsed.isBefore(instant)) {
           NewsletterTask(
             UUID.fromString(dbTask.id),
             dbTask.email,
             dbTask.lang.validate[Lang] match {
               case JsSuccess(value, _) => value
               case e: JsError => Lang("en")
             },
             parsed,
             dbTask.newsletterFashion,
             dbTask.newsletterFineJewelry,
             dbTask.newsletterHomeCollection
           ) +: taskSeq
         } else {
           NewsletterTask(
             UUID.fromString(dbTask.id),
             dbTask.email,
             dbTask.lang.validate[Lang] match {
               case JsSuccess(value, _) => value
               case e: JsError => Lang("en")
             },
             parsed,
             dbTask.newsletterFashion,
             dbTask.newsletterFineJewelry,
             dbTask.newsletterHomeCollection
           ) +: someTasks
         }
       }
     }
     Future.successful(taskSeq)
   }

  /**
   * Saves a newsletter.
   *
   * @param newsletter The newsletter to save.
   * @return The saved newsletter.
   */
   def save(task: NewsletterTask): Future[NewsletterTask] = {
     println(s"in def save(task: NewsletterTask): $NewsletterTask")
     val dbNewsletterTask = DbNewsletterTask(
       task.id.toString,
       task.email,
       Json.toJson(task.lang),
       Json.toJson(task.expiry),
       task.newsletterFashion,
       task.newsletterFineJewelry,
       task.newsletterHomeCollection
     )
     println(s"dbNewsletterTask: $dbNewsletterTask")
     db.run(tasks.insertOrUpdate(dbNewsletterTask)).map(_ => task)
   }

  /**
   * Removes the newsletter task for the given ID.
   *
   * @param id The ID for which the newsletter task should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Int] =
    db.run(tasks.filter(_.id === id.toString).delete)
}
