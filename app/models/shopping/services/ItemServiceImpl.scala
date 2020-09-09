package models.shopping.services

import java.time.Clock

import models.core.Item
import models.shopping.daos.ItemDAO
import utils.core.CustomerExecutionContext
import javax.inject.Inject
import java.util.UUID

import scala.concurrent.Future

/**
 * Handles actions to items.
 *
 * @param itemDAO The item DAO implementation.
 * @param clock      The clock instance.
 * @param ex         The execution context.
 */
class ItemServiceImpl @Inject() (itemDAO: ItemDAO, clock: Clock)(
  implicit
  ex: CustomerExecutionContext
) extends ItemService {

  /**
   * Retrieves a item that matches the specified ID.
   *
   * @param id The ID to retrieve a item.
   * @param category The category of the item to find.
   * @return The retrieved item or None if no item could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[Item]] = itemDAO.find(id)

  /**
   * Saves a item.
   *
   * @param item The item to save.
   * @return The saved item.
   */
  def save(item: Item): Future[Item] = itemDAO.save(item)

  /**
   * Retrieves all the items in a collection.
   */
  def retrieveAll: Future[Seq[Item]] = itemDAO.findAll

}
