package models.shopping.daos

import java.time.{Clock, Instant}
import java.util.UUID
import javax.inject.{ Inject, Singleton }

import models.core.{
  AgeLimit, CardData, Newsletter,
  NewsletterFashion, NewsletterFineJewelry, NewsletterHomeCollection,
  Order, RefundTransaction, Registration, ResponseData, Settings,
  Transaction, User, VoidTransaction
}
import models.shopping.tables._
import util.MyPostgresDriver
import utils.core.CustomerExecutionContext
import utils.formats.Formats._

import org.joda.time.DateTime

import com.mohiva.play.silhouette.api.LoginInfo

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.i18n.Lang
import play.api.libs.json._

import scala.concurrent.Future

/**
 * Give access to the [[Transaction]] object.
 *
 * @param ec               The execution context.
 */
 @Singleton
 class TransactionDAO @Inject() (
   protected val dbConfigProvider: DatabaseConfigProvider,
   clock: Clock
 )(implicit ec: CustomerExecutionContext) extends TransactionComponent with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val transactions = TableQuery[TransactionTable]

  private def createTransaction: Transaction = Transaction(
    UUID.randomUUID(),
    clock.instant(),
    Order(
      UUID.randomUUID(),
      None,
      None,
      None,
      "Order Status"
    ),
    User(
      UUID.randomUUID(),
      LoginInfo("Credentials", "Email"),
      None,
      None,
      None,
      None,
      None,
      Registration(
        Lang("en"),
        "IP",
        None,
        None,
        DateTime.now()
      ),
      Settings(Lang("en")),
      AgeLimit(false),
      None,
      None,
      None,
      None,
      None,
      Newsletter(
        NewsletterFashion(false),
        NewsletterFineJewelry(false),
        NewsletterHomeCollection(false)
      ),
      None,
      None,
      None,
      None
    ),
    CardData(
      "last 4 digits",
      "expiry date"
    ),
    None,
    None,
    None,
    false
  )

  /**
   * Finds a Transaction by its id.
   *
   * @param id The ID of the Transaction to find.
   * @return The found Transaction or None if no transaction for the given ID could be found.
   */
  def find(id: UUID): Future[Option[Transaction]] = {
    val transactionQuery = for {
      dbTransaction <- transactions.filter(_.id === id)
    } yield dbTransaction

    db.run(transactionQuery.result.headOption).map { dbTransactionOption =>
      dbTransactionOption.map {
        case transaction => transaction.content.validate[Transaction] match {
          case JsSuccess(value, _) => value
          case e: JsError => createTransaction
        }
      }
    }
  }


  /**
   * Retrieves all the Transactions in a collection.
   */
  def findAll: Future[Seq[Transaction]] = {
    val transactionSeq = Seq[Transaction]()
    val transactionQuery = for {
      dbTransaction <- transactions
    } yield dbTransaction

    db.run(transactionQuery.result.headOption).map { dbTransactionOption =>
      dbTransactionOption.map {
        case transaction => transaction.content.validate[Transaction] match {
          case JsSuccess(value, _) => value +: transactionSeq
          case e: JsError => createTransaction
        }
      }
    }
    Future.successful(transactionSeq)
  }

  /**
   * Saves a Transaction.
   *
   * @param user The order to save.
   * @return The saved Transaction.
   */
  def save(transaction: Transaction): Future[Transaction] = {
    val dbTransaction = DbTransaction(
      transaction.id,
      Json.toJson(transaction)
    )
    db.run(transactions.insertOrUpdate(dbTransaction)).map(_ => transaction)
  }

  /**
   * Deletes the Transaction for the given ID.
   *
   * @param id The ID for which the Transaction should be removed.
   * @return A future to wait for the process to be completed.
   */
  def delete(id: UUID): Future[Int] =
    db.run(transactions.filter(_.id === id).delete)

}
