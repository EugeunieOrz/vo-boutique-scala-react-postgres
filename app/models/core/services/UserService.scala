package models.core.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import models.core.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Deletes a user's account.
   *
   * @param id The id of the user.
   * @return A future to wait for the process to be completed.
   */
  def deleteAccount(id: UUID): Future[Int]

}
