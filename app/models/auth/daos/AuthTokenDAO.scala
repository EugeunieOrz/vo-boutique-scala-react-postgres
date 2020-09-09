package models.auth.daos

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.JsValue
import util.MyPostgresDriver
import utils.core.CustomerExecutionContext

import scala.concurrent.Future
import org.joda.time.DateTime
import models.auth.AuthToken
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider, HasDatabaseConfig}

case class DbAuthToken(
  id: String,
  userID: String,
  expiry: String
)

trait AuthTokenComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class AuthTokenTable(tag: Tag) extends Table[DbAuthToken](tag, "auth_tokens") {

    def id: Rep[String] = column[String]("id", O.PrimaryKey)
    def userID: Rep[String] = column[String]("user_id")
    def expiry: Rep[String] = column[String]("expiry")

    def * = (id, userID, expiry) <> (DbAuthToken.tupled, DbAuthToken.unapply)

  }
}

/**
 * Give access to the [[AuthToken]] object.
 */
@Singleton
class AuthTokenDAO @Inject()
(protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit ec: CustomerExecutionContext
) extends AuthTokenComponent
with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val tokens = TableQuery[AuthTokenTable]

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] = {
    db.run(tokens.filter(_.id === id.toString).result.headOption).map { resultOption =>
      resultOption.map {
        dbToken => AuthToken(UUID.fromString(dbToken.id), UUID.fromString(dbToken.userID), DateTime.parse(dbToken.expiry))
      }
    }
  }

  /**
   * Finds expired tokens.
   *
   * @param instant The current instant.
   */
  def findExpired(dateTime: DateTime): Future[Seq[AuthToken]] = {

    val tokenSeq = Seq[AuthToken]()
    val tokenQuery = for {
      token <- tokens
    } yield token

    db.run(tokenQuery.result).map { tokenOption =>
      tokenOption.flatMap {
        case token =>
        AuthToken(
          UUID.fromString(token.id),
          UUID.fromString(token.userID),
          DateTime.parse(token.expiry)
        ) +: tokenSeq
      }
    }
    Future.successful(tokenSeq.filter(t => t.expiry.isBefore(dateTime)))
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    db.run((
      tokens returning tokens.map(_.id) into ((token, id) => token.copy(id))
    ) += DbAuthToken(token.id.toString, token.userID.toString, token.expiry.toString)).map { _ =>
      token
    }
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Int] = {
    db.run(tokens.filter(_.id === id.toString).delete)
  }
}
