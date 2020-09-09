package models.core.services

import java.util.UUID
import javax.inject.Inject

import org.joda.time.DateTime
import play.api.i18n.Lang

import com.mohiva.play.silhouette.api.LoginInfo
import models.core.User
import models.core.daos.UserDAO
import utils.core.CustomerExecutionContext

//import scala.concurrent.ExecutionContext.Implicits.global._
import scala.concurrent.Future

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO)(
  implicit
  ex: CustomerExecutionContext
) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = userDAO.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.save(user)

  /**
   * Deletes a user's account.
   *
   * @param id The id of the user.
   * @return A future to wait for the process to be completed.
   */
  def deleteAccount(id: UUID): Future[Int] = userDAO.removeAccount(id)

}
