package models.auth.daos

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import models.core.daos.UserDAO
import util.MyPostgresDriver
import utils.core.CustomerExecutionContext

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider, HasDatabaseConfig}

import scala.concurrent.Future

case class DbPasswordInfo(
  userId: String,
  password: String,
  hasher: String,
  salt: Option[String],
)

trait PasswordInfoComponent { self: HasDatabaseConfig[MyPostgresDriver] =>
  import profile.api._

  class PasswordInfoTable(tag: Tag) extends Table[DbPasswordInfo](tag, "password_info") {

    def userId: Rep[String] = column[String]("user_id")
    def password: Rep[String] = column[String]("password")
    def hasher: Rep[String] = column[String]("hasher")
    def salt: Rep[Option[String]] = column[Option[String]]("salt")

    def * = (userId, password, hasher, salt) <> (DbPasswordInfo.tupled, DbPasswordInfo.unapply)

  }
}

class PasswordInfoDAO @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  userDAO: UserDAO
)(implicit ec: CustomerExecutionContext)
extends DelegableAuthInfoDAO[PasswordInfo]
with PasswordInfoComponent with HasDatabaseConfigProvider[MyPostgresDriver] {

  import profile.api._

  private val passwordInfos = TableQuery[PasswordInfoTable]

  def passwordInfoQuery(loginInfo: LoginInfo): Query[PasswordInfoTable, DbPasswordInfo, Seq] = {
    for {
      dbLoginInfo <- userDAO.loginInfoQuery(loginInfo)
      dbPasswordInfo <- passwordInfos.filter(_.userId === dbLoginInfo.userId)
    } yield dbPasswordInfo
  }

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(passwordInfoQuery(loginInfo).result.headOption).map { dbPasswordInfoOption =>
      dbPasswordInfoOption.map {
        dbPasswordInfo => PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)
      }
    }
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    db.run(userDAO.loginInfoQuery(loginInfo).result.headOption.map { dbLoginInfoOption =>
      dbLoginInfoOption.map {
        dbLoginInfo =>
          {
            val dbPasswordInfo = DbPasswordInfo(dbLoginInfo.userId, authInfo.password, authInfo.hasher, authInfo.salt)
            db.run(passwordInfos += dbPasswordInfo)
          }
      }
    }).map { _ =>
      authInfo
    }
  }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    db.run(userDAO.loginInfoQuery(loginInfo).result.headOption.map { dbLoginInfoOption =>
      dbLoginInfoOption.map {
        dbLoginInfo =>
          {
            db.run {
              passwordInfos.filter(_.userId === dbLoginInfo.userId)
                .map(p => (p.userId, p.password, p.hasher, p.salt))
                .update((dbLoginInfo.userId, authInfo.password, authInfo.hasher, authInfo.salt))
                .transactionally
            }
          }
      }
    }).map { _ =>
      authInfo
    }
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    db.run(userDAO.loginInfoQuery(loginInfo).result.headOption.map { dbLoginInfoOption =>
      dbLoginInfoOption.map {
        dbLoginInfo =>
          {
            val dbPasswordInfo = DbPasswordInfo(dbLoginInfo.userId, authInfo.password, authInfo.hasher, authInfo.salt)
            db.run(passwordInfoQuery(loginInfo).insertOrUpdate(dbPasswordInfo).transactionally)
          }
      }
    }).map { _ =>
      authInfo
    }
  }

  def remove(loginInfo: LoginInfo): Future[Unit] = {
    db.run(passwordInfoQuery(loginInfo).delete).map(_ => Unit)
  }
}
