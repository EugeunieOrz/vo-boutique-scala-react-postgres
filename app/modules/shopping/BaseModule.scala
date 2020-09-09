package modules.shopping

import models.shopping.daos._
import models.shopping.services._
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * The Guice `Main` module.
 */
class BaseModule extends ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind(classOf[ItemDAO]).asEagerSingleton()
    bind[ItemService].to[ItemServiceImpl]
    bind(classOf[OrderDAO]).asEagerSingleton()
    bind[OrderService].to[OrderServiceImpl]
    bind(classOf[TransactionDAO]).asEagerSingleton()
    bind[TransactionService].to[TransactionServiceImpl]
  }
}
