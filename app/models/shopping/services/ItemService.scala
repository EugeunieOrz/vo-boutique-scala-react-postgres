package models.shopping.services

import models.core.Item
import java.util.UUID

import scala.concurrent.Future

/**
 * Handles actions to items.
 */
trait ItemService {

  /**
   * Retrieves a item that matches the specified ID.
   *
   * @param id The ID to retrieve a item.
   * @param category The category of the item to retrieve.
   * @return The retrieved item or None if no item could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[Item]]

  /**
   * Saves a item.
   *
   * @param item The item to save.
   * @return The saved item.
   */
  def save(item: Item): Future[Item]

  /**
   * Retrieves all the items in a collection.
   */
  def retrieveAll: Future[Seq[Item]]

}
