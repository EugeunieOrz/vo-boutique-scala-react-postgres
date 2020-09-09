package controllers.shopping

/** Author: Ievgeniia Ozirna
  *
  * Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
  */

import models.core.{
  Item,
  Order,
  OrderNumber,
  OrderToReturn,
  OrderToReturn2,
  RefundTransaction,
  RefundResponseData,
  TransactionID,
  ResponseCode,
  AuthCode,
  Size,
  Transaction,
  User,
  VoidTransaction
}
import models.core.services.UserService
import models.shopping.services.{ ItemService, OrderService, TransactionService }
import utils.core.JSRouter
import utils.core.SomeMethods._
import utils.formats.Formats._
import utils.silhouette.{ BoutiqueEnv, CustomerController }

import java.math.BigDecimal
import java.time.{Clock, LocalDate, ZoneId}
import java.util.UUID
import javax.inject.Inject

import net.ceedubs.ficus.Ficus._

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import org.joda.time.DateTime

import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.json._
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc._
import play.api.Configuration

import net.authorize.Environment
import net.authorize.api.contract.v1._
import net.authorize.api.controller.base.ApiOperationBase
import net.authorize.api.controller.CreateTransactionController

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Order` controller.
 *
 * @param controllerComponents    The Play controller components.
 * @param productService          The products service.
 * @param productInTheBagService  The services of adding products to the shopping bag.
 * @param silhouette              The Silhouette stack.
 * @param ex                      The execution context.
 */
class OrderController @Inject() (
  val controllerComponents: ControllerComponents,
  val silhouette: Silhouette[BoutiqueEnv[User]],
  orderService: OrderService,
  itemService: ItemService,
  mailerClient: MailerClient,
  messagesApi: MessagesApi,
  transactionService: TransactionService,
  userService: UserService,
  clock: Clock,
  config: Configuration,
)(
  implicit
  ex: ExecutionContext
) extends CustomerController {

  private val loginId = config.underlying.getString("gateway.api.login.id")
  private val transKey = config.underlying.getString("gateway.transaction.key")

  ApiOperationBase.setEnvironment(Environment.SANDBOX)
  private val merchAuthType: MerchantAuthenticationType = new MerchantAuthenticationType()
  merchAuthType.setName(loginId)
  merchAuthType.setTransactionKey(transKey)
  ApiOperationBase.setMerchantAuthentication(merchAuthType)

  def followOrder: Action[JsValue] = Action(parse.json).async {
    implicit request =>
      request.body.validate[OrderNumber]
        .fold(
          errors =>
            Future.successful(
              BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
          data => orderService.retrieve(data.orderNumber).flatMap {
            case Some(order) => Future.successful(Ok(ApiResponse(
              "request.ok",
              Messages("request.ok"),
              Json.toJson(order)
            )))
            case None => Future.successful(Ok(ApiResponse(
              "shopping.order.not.found",
              Messages("shopping.order.not.found")
            )))
          }
        )
  }

  def fillReturnForm: Action[JsValue] = UnsecuredAction(parse.json).async {
    implicit request =>
      request.body.validate[OrderNumber]
        .fold(
          errors =>
            Future.successful(
              BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
          data => orderService.retrieve(data.orderNumber).flatMap {
            case Some(order) =>
              if(order.status != "Order Returned") {
                transactionService.retrieve(order.id).flatMap {
                  case Some(transaction) =>
                    val dt1 = transaction.dateTime
                                         .atZone(ZoneId.of("America/Los_Angeles"))
                                         .withHour(15)
                                         .withMinute(0)
                                         .withSecond(0)
                                         .toInstant
                    println(s"6 pm PST at the day of transaction: $dt1")
                    val dt2 = transaction.dateTime.atZone(ZoneId.of("America/Los_Angeles")).toInstant
                    println(s"transaction date time PST: $dt2")
                    val dt3 = clock.instant().atZone(ZoneId.of("America/Los_Angeles")).toInstant
                    println(s"time now is after the 3 pm PST at the day of transaction: ${dt3.isAfter(dt1)}")
                    if(dt3.isAfter(dt1)) {
                      transactionService.save(transaction.copy(
                        settled = true
                      )).map { _ =>
                        Ok(ApiResponse(
                          "transaction.was.settled",
                          Messages("transaction.was.settled"),
                          Json.toJson(order)
                        ))
                      }
                    } else {
                      transactionService.save(transaction.copy(
                        settled = false
                      )).map { _ =>
                        Ok(ApiResponse(
                          "transaction.not.settled",
                          Messages("transaction.not.settled"),
                          Json.toJson(order)
                        ))
                      }
                    }
                  case None => Future.successful(Ok(ApiResponse(
                    "transaction.not.found",
                    Messages("transaction.not.found")
                  )))
                }
              } else {
                // order has already been returned to the customer
                Future.successful(Ok(ApiResponse(
                  "shopping.order.returned",
                  Messages("shopping.order.returned")
                )))
              }
            case None => Future.successful(Ok(ApiResponse(
              "shopping.order.not.found",
              Messages("shopping.order.not.found")
            )))
          }
        )
  }

  def fillReturnForm2: Action[JsValue] = SecuredAction(parse.json).async {
    implicit request =>
      request.body.validate[OrderNumber]
        .fold(
          errors =>
            Future.successful(
              BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
          data => orderService.retrieve(data.orderNumber).flatMap {
            case Some(order) =>
            if(order.status != "Order Returned") {
              transactionService.retrieve(order.id).flatMap {
                case Some(transaction) =>
                  val dt1 = transaction.dateTime
                                       .atZone(ZoneId.of("America/Los_Angeles"))
                                       .withHour(18)
                                       .withMinute(0)
                                       .withSecond(0)
                                       .toInstant
                  println(s"6 pm PST at the day of transaction: $dt1")
                  val dt2 = transaction.dateTime.atZone(ZoneId.of("America/Los_Angeles")).toInstant
                  println(s"transaction date time PST: $dt2")
                  val dt3 = clock.instant().atZone(ZoneId.of("America/Los_Angeles")).toInstant
                  println(s"time now is after the 6 pm PST at the day of transaction: ${dt3.isAfter(dt1)}")
                  if(dt3.isAfter(dt1)) {
                    transactionService.save(transaction.copy(
                      settled = true
                    )).map { _ =>
                      Ok(ApiResponse(
                        "transaction.was.settled",
                        Messages("transaction.was.settled"),
                        Json.toJson(order)
                      ))
                    }
                  } else {
                    transactionService.save(transaction.copy(
                      settled = false
                    )).map { _ =>
                      Ok(ApiResponse(
                        "transaction.not.settled",
                        Messages("transaction.not.settled"),
                        Json.toJson(order)
                      ))
                    }
                  }
                case None => Future.successful(Ok(ApiResponse(
                  "transaction.not.found",
                  Messages("transaction.not.found")
                )))
              }
            } else {
              // order has already been returned to the customer
              Future.successful(Ok(ApiResponse(
                "shopping.order.returned",
                Messages("shopping.order.returned")
              )))
            }
            case None => Future.successful(Ok(ApiResponse(
              "shopping.order.not.found",
              Messages("shopping.order.not.found")
            )))
          }
        )
  }

  def fillReturnProduct: Action[JsValue] = UnsecuredAction(parse.json).async {
    implicit request =>
      request.body.validate[OrderToReturn]
        .fold(
          errors =>
            Future.successful(
              BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
          data => orderService.retrieve(data.id).flatMap {
            case Some(order) =>
              if(order.status != "Order Returned") {
                transactionService.retrieve(order.id).flatMap {
                  case Some(transaction) => userService.retrieve(transaction.customer.userID).flatMap {
                    case Some(user) =>
                      // check if a customer has this particular order for refund
                      user.orders match {
                        case Some(ordersSeq) =>
                          if(data.items.contains("id1")) {
                            // transaction has not been settled yet, so it can only be voided
                            println("transaction has not been settled yet, so it can only be voided")
                            val response = voidTransaction(transaction)
                            if(response != null &&
                               response.getMessages().getResultCode() == MessageTypeEnum.OK) {
                                 val index = ordersSeq.indexOf(order)
                                 println(s"index of order is $index")
                                 val updated = removeElementFromSeq(ordersSeq, index)

                                 for {
                                   _ <- orderService.save(order.copy(status = "Order Returned"))
                                   _ <- transactionService.save(transaction.copy(order = order.copy(
                                     status = "Order Returned"
                                   )))
                                   _ <- userService.save(user.copy(orders = Some(updated)))
                                 } yield {
                                   val lang = user.settings.lang
                                   user.email.map { email =>
                                     mailerClient.send(Email(
                                       subject = messagesApi("return.order.confirmation.subject")(lang),
                                       from = messagesApi("email.from")(lang),
                                       to = Seq(email),
                                       bodyText = Some(
                                         twirl.shopping.views.txt.emails.orderReturnConfirm(
                                           user, order)(messagesApi, lang).body),
                                       bodyHtml = Some(
                                         twirl.shopping.views.html.emails.orderReturnConfirm(
                                           user, order)(messagesApi, lang).body)
                                     ))
                                   }
                                 }

                                 Future.successful(Ok(ApiResponse(
                                   "void.transaction.approved",
                                   Messages("void.transaction.approved")
                                 )))
                              } else {
                              Future.successful(BadRequest(ApiResponse(
                                "void.transaction.declined",
                                Messages("void.transaction.declined")
                              )))
                            }
                            // transaction has not been settled yet, so it can only be voided
                          } else {
                            // transaction has been settled, so it can be refunded
                            println("transaction has been settled, so it can be refunded")
                            val amount = data.items.map(i => i.split(" ")(2).toDouble).foldLeft(0.0)(_ + _)
                            println(s"total amount to refund: $amount")
                            // check the response
                            val response = refundTransaction(transaction, amount)
                            if (response != null &&
                              response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                              // check if a customer wants a partial or full refund
                              order.shoppingBag match {
                                case Some(bag) => {
                                  println(s"bag.total = ${bag.total}")
                                  println(s"amount = $amount")
                                  if(bag.total == amount) {

                                    val index = ordersSeq.indexOf(order)
                                    val updated = removeElementFromSeq(ordersSeq, index)

                                    for {
                                      _ <- orderService.save(order.copy(status = "Order Returned"))
                                      _ <- transactionService.save(transaction.copy(order = order.copy(
                                        status = "Order Returned"
                                      )))
                                      _ <- userService.save(user.copy(orders = Some(updated)))
                                    } yield {
                                      val lang = user.settings.lang
                                      user.email.map { email =>
                                        mailerClient.send(Email(
                                          subject = messagesApi("return.order.confirmation.subject")(lang),
                                          from = messagesApi("email.from")(lang),
                                          to = Seq(email),
                                          bodyText = Some(
                                            twirl.shopping.views.txt.emails.orderReturnConfirm(
                                              user, order)(messagesApi, lang).body),
                                          bodyHtml = Some(
                                            twirl.shopping.views.html.emails.orderReturnConfirm(
                                              user, order)(messagesApi, lang).body)
                                        ))
                                      }
                                    }

                                  } else {
                                    println("a partial refund")
                                    if(data.items.length > 1) {
                                      println("more than one item to remove from orders and return to products")
                                      data.items.map { i =>
                                        val itemID = i.split(" ")(0)
                                        val itemSize = i.split(" ")(1)
                                        val itemPrice = i.split(" ")(2).toDouble
                                        removeItemFromOrders(itemID, itemSize, itemPrice, order, ordersSeq, user)
                                      }
                                      transactionService.save(transaction.copy(order = order.copy(
                                        status = "Order Partially Refunded"
                                      )))
                                    } else {
                                      println("only one item to remove from orders and return to products")
                                      val item = data.items(0)
                                      val itemID = item.split(" ")(0)
                                      val itemSize = item.split(" ")(1)
                                      val itemPrice = item.split(" ")(2).toDouble
                                      removeItemFromOrders(itemID, itemSize, itemPrice, order, ordersSeq, user)
                                      transactionService.save(transaction.copy(order = order.copy(
                                        status = "Order Partially Refunded"
                                      )))
                                    }
                                    // send email about partial refund
                                    val lang = user.settings.lang
                                    user.email.map { email =>
                                      mailerClient.send(Email(
                                        subject = messagesApi("return.order.confirmation.subject")(lang),
                                        from = messagesApi("email.from")(lang),
                                        to = Seq(email),
                                        bodyText = Some(
                                          twirl.shopping.views.txt.emails.partialRefundConfirm(
                                            user, order, amount)(messagesApi, lang).body),
                                        bodyHtml = Some(
                                          twirl.shopping.views.html.emails.partialRefundConfirm(
                                            user, order, amount)(messagesApi, lang).body)
                                      ))
                                    }
                                  }
                                  Future.successful(Ok(ApiResponse(
                                    "refund.transaction.approved",
                                    Messages("refund.transaction.approved")
                                  )))
                                }
                                case None =>
                                //if a customer does not have any orders history
                                Future.successful(
                                  BadRequest(ApiResponse(
                                    "shopping.order.not.found",
                                    Messages("shopping.order.not.found")
                                  ))
                                )
                              }

                            } else {
                              // if refund transaction was declined, then void the transaction
                              val response = voidTransaction(transaction)
                              if(response != null &&
                                 response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                                   val index = ordersSeq.indexOf(order)
                                   val updated = removeElementFromSeq(ordersSeq, index)

                                   for {
                                     _ <- orderService.save(order.copy(status = "Order Returned"))
                                     _ <- transactionService.save(transaction.copy(order = order.copy(
                                       status = "Order Returned"
                                     )))
                                     _ <- userService.save(user.copy(
                                       orders = Some(updated)
                                     ))
                                   } yield {
                                     val lang = user.settings.lang
                                     user.email.map { email =>
                                       mailerClient.send(Email(
                                         subject = messagesApi("return.order.confirmation.subject")(lang),
                                         from = messagesApi("email.from")(lang),
                                         to = Seq(email),
                                         bodyText = Some(
                                           twirl.shopping.views.txt.emails.orderReturnConfirm(
                                             user, order)(messagesApi, lang).body),
                                         bodyHtml = Some(
                                           twirl.shopping.views.html.emails.orderReturnConfirm(
                                             user, order)(messagesApi, lang).body)
                                       ))
                                     }
                                   }

                                   Future.successful(Ok(ApiResponse(
                                     "void.transaction.approved",
                                     Messages("void.transaction.approved")
                                   )))
                                } else {
                                Future.successful(BadRequest(ApiResponse(
                                  "void.transaction.declined",
                                  Messages("void.transaction.declined")
                                )))
                              }
                            }
                            // transaction has been settled, so it can be refunded
                          }
                        case None =>
                          //if a customer does not have any orders history
                          Future.successful(
                            BadRequest(ApiResponse(
                              "shopping.order.not.found",
                              Messages("shopping.order.not.found")
                            ))
                          )
                      }
                    case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
                  }.recover {
                    case _: ProviderException =>
                      BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
                  }
                  case None => Future.successful(BadRequest(ApiResponse(
                    "transaction.not.found",
                    Messages("transaction.not.found")
                  )))
                }
              } else {
                // order has already been retuned by the customer
                Future.successful(BadRequest(ApiResponse(
                  "shopping.order.returned",
                  Messages("shopping.order.returned")
                )))
              }
            case None => Future.successful(BadRequest(ApiResponse(
              "shopping.order.not.found",
              Messages("shopping.order.not.found")
            )))
          }
        )
  }

  def fillReturnProduct2: Action[JsValue] = SecuredAction(parse.json).async {
    implicit request =>
      request.body.validate[OrderToReturn2]
        .fold(
          errors =>
            Future.successful(
              BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))),
          data => orderService.retrieve(data.id).flatMap {
            case Some(order) =>
            if(order.status != "Order Returned") {
              transactionService.retrieve(order.id).flatMap {
                case Some(transaction) => userService.retrieve(transaction.customer.userID).flatMap {
                  case Some(user) =>
                    // check if a customer has this particular order for refund
                    user.orders match {
                      case Some(ordersSeq) =>
                        if(data.items.exists(i => i == "id1")) {
                          // transaction has not been settled yet, so it can only be voided
                          val response = voidTransaction(transaction)
                          if(response != null &&
                             response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                               val index = ordersSeq.indexOf(order)
                               println(s"index = $index")
                               val updated = removeElementFromSeq(ordersSeq, index)

                               for {
                                 _ <- orderService.save(order.copy(status = "Order Returned"))
                                 _ <- transactionService.save(transaction.copy(order = order.copy(
                                   status = "Order Returned"
                                 )))
                                 _ <- userService.save(user.copy(
                                   orders = Some(updated)
                                 ))
                               } yield {
                                 val lang = user.settings.lang
                                 user.email.map { email =>
                                   mailerClient.send(Email(
                                     subject = messagesApi("return.order.confirmation.subject")(lang),
                                     from = messagesApi("email.from")(lang),
                                     to = Seq(email),
                                     bodyText = Some(
                                       twirl.shopping.views.txt.emails.orderReturnConfirm(
                                         user, order)(messagesApi, lang).body),
                                     bodyHtml = Some(
                                       twirl.shopping.views.html.emails.orderReturnConfirm(
                                         user, order)(messagesApi, lang).body)
                                   ))
                                 }
                               }

                               Future.successful(Ok(ApiResponse(
                                 "void.transaction.approved",
                                 Messages("void.transaction.approved")
                               )))
                            } else {
                            Future.successful(BadRequest(ApiResponse(
                              "void.transaction.declined",
                              Messages("void.transaction.declined")
                            )))
                          }
                          // transaction has not been settled yet, so it can only be voided
                        } else {
                          // transaction has been settled, so it can be refunded
                          val amount = data.items.map(i => i.split(" ")(2).toDouble).foldLeft(0.0)(_ + _)
                          println(s"total amount to refund: $amount")
                          // check the response
                          val response = refundTransaction(transaction, amount)
                          if (response != null &&
                            response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                            // check if a customer wants a partial or full refund
                            order.shoppingBag match {
                              case Some(bag) =>
                                if(bag.total == amount) {

                                  val index = ordersSeq.indexOf(order)
                                  val updated = removeElementFromSeq(ordersSeq, index)

                                  for {
                                    _ <- orderService.save(order.copy(status = "Order Returned"))
                                    _ <- transactionService.save(transaction.copy(order = order.copy(
                                      status = "Order Returned"
                                    )))
                                    _ <- userService.save(user.copy(
                                      orders = Some(updated)
                                    ))
                                  } yield {
                                    val lang = user.settings.lang
                                    user.email.map { email =>
                                      mailerClient.send(Email(
                                        subject = messagesApi("return.order.confirmation.subject")(lang),
                                        from = messagesApi("email.from")(lang),
                                        to = Seq(email),
                                        bodyText = Some(
                                          twirl.shopping.views.txt.emails.orderReturnConfirm(
                                            user, order)(messagesApi, lang).body),
                                        bodyHtml = Some(
                                          twirl.shopping.views.html.emails.orderReturnConfirm(
                                            user, order)(messagesApi, lang).body)
                                      ))
                                    }
                                  }

                                } else {
                                  println("a partial refund")
                                  if(data.items.length > 1) {
                                    println("more than one item to remove from orders and return to products")
                                    data.items.map { i =>
                                      val itemID = i.split(" ")(0)
                                      val itemSize = i.split(" ")(1)
                                      val itemPrice = i.split(" ")(2).toDouble
                                      removeItemFromOrders(itemID, itemSize, itemPrice, order, ordersSeq, user)
                                    }
                                    transactionService.save(transaction.copy(order = order.copy(
                                      status = "Order Partially Refunded"
                                    )))
                                  } else {
                                    println("only one item to remove from orders and return to products")
                                    val item = data.items(0)
                                    val itemID = item.split(" ")(0)
                                    val itemSize = item.split(" ")(1)
                                    val itemPrice = item.split(" ")(2).toDouble
                                    removeItemFromOrders(itemID, itemSize, itemPrice, order, ordersSeq, user)
                                    transactionService.save(transaction.copy(order = order.copy(
                                      status = "Order Partially Refunded"
                                    )))
                                  }
                                  // send email about partial refund
                                  val lang = user.settings.lang
                                  user.email.map { email =>
                                    mailerClient.send(Email(
                                      subject = messagesApi("return.order.confirmation.subject")(lang),
                                      from = messagesApi("email.from")(lang),
                                      to = Seq(email),
                                      bodyText = Some(
                                        twirl.shopping.views.txt.emails.partialRefundConfirm(
                                          user, order, amount)(messagesApi, lang).body),
                                      bodyHtml = Some(
                                        twirl.shopping.views.html.emails.partialRefundConfirm(
                                          user, order, amount)(messagesApi, lang).body)
                                    ))
                                  }
                                }
                                Future.successful(Ok(ApiResponse(
                                  "refund.transaction.approved",
                                  Messages("refund.transaction.approved")
                                )))
                              case None => Future.successful(BadRequest(ApiResponse(
                                "void.transaction.declined",
                                Messages("void.transaction.declined")
                              )))

                            }

                          } else {
                            // if refund transaction was declined, then void the transaction
                            val response = voidTransaction(transaction)
                            if(response != null &&
                               response.getMessages().getResultCode() == MessageTypeEnum.OK) {

                                 val index = ordersSeq.indexOf(order)
                                 val updated = removeElementFromSeq(ordersSeq, index)

                                 for {
                                   _ <- orderService.save(order.copy(status = "Order Returned"))
                                   _ <- transactionService.save(transaction.copy(order = order.copy(
                                     status = "Order Returned"
                                   )))
                                   _ <- userService.save(user.copy(
                                     orders = Some(updated)
                                   ))
                                 } yield {
                                   val lang = user.settings.lang
                                   user.email.map { email =>
                                     mailerClient.send(Email(
                                       subject = messagesApi("return.order.confirmation.subject")(lang),
                                       from = messagesApi("email.from")(lang),
                                       to = Seq(email),
                                       bodyText = Some(
                                         twirl.shopping.views.txt.emails.orderReturnConfirm(
                                           user, order)(messagesApi, lang).body),
                                       bodyHtml = Some(
                                         twirl.shopping.views.html.emails.orderReturnConfirm(
                                           user, order)(messagesApi, lang).body)
                                     ))
                                   }
                                 }

                                 Future.successful(Ok(ApiResponse(
                                   "void.transaction.approved",
                                   Messages("void.transaction.approved")
                                 )))
                              } else {
                              Future.successful(BadRequest(ApiResponse(
                                "void.transaction.declined",
                                Messages("void.transaction.declined")
                              )))
                            }
                          }
                          // transaction has been settled, so it can be refunded
                        }
                      case None =>
                        //if a customer does not have any orders history
                        Future.successful(
                          BadRequest(ApiResponse(
                            "shopping.order.not.found",
                            Messages("shopping.order.not.found")
                          ))
                        )
                    }
                  case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
                }.recover {
                  case _: ProviderException =>
                    BadRequest(ApiResponse("user.not.found", Messages("user.not.found")))
                }
                case None => Future.successful(BadRequest(ApiResponse(
                  "transaction.not.found",
                  Messages("transaction.not.found")
                )))
              }
            } else {
              // order has already been retuned by the customer
              Future.successful(BadRequest(ApiResponse(
                "shopping.order.returned",
                Messages("shopping.order.returned")
              )))
            }
            case None => Future.successful(BadRequest(ApiResponse(
              "shopping.order.not.found",
              Messages("shopping.order.not.found")
            )))
          }
        )
  }

  private def refundTransaction(transaction: Transaction, amount: Double): ANetApiResponse = {
    // Create a payment object, last 4 of the credit card and expiration date are required
    val paymentType: PaymentType = new PaymentType()
    val creditCard: CreditCardType = new CreditCardType()
    creditCard.setCardNumber(transaction.cardData.lastFourDigits)
    creditCard.setExpirationDate("XXXX")
    paymentType.setCreditCard(creditCard)

    // Create the payment transaction request
    val txnRequest: TransactionRequestType = new TransactionRequestType()
    txnRequest.setTransactionType(TransactionTypeEnum.REFUND_TRANSACTION.value())
    txnRequest.setRefTransId(transaction.responseData.get.transactionID.code)
    txnRequest.setAmount(new BigDecimal(amount.toString()))
    txnRequest.setPayment(paymentType)

    // Make the API Request
    val apiRequest: CreateTransactionRequest = new CreateTransactionRequest()
    apiRequest.setTransactionRequest(txnRequest)
    val controller: CreateTransactionController = new CreateTransactionController(apiRequest)
    controller.execute()

    val response: CreateTransactionResponse = controller.getApiResponse()

    if (response != null) {
      // If API Response is ok, go ahead and check the transaction response
      if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
        val result: TransactionResponse = response.getTransactionResponse()
        if (result.getMessages() != null) {

          val transId = result.getTransId()
          val respCode = result.getResponseCode()
          val msgCode = result.getMessages().getMessage().get(0).getCode()
          val descr = result.getMessages().getMessage().get(0).getDescription()
          val authCode = result.getAuthCode()

          transactionService.save(transaction.copy(
            refundTransaction = Some(RefundTransaction(
              id = UUID.randomUUID(),
              dateTime = clock.instant(),
              responseData = Some(RefundResponseData(
                transactionID = TransactionID(transId),
                responseCode = Some(ResponseCode(respCode)),
                messageCode = Some(msgCode),
                description = Some(descr),
                authCode = Some(AuthCode(authCode)),
                errorCode = None,
                errorMessage = None
              ))
            ))
          ))

          println(s"Successfully created transaction with Transaction ID: $transId")
          println(s"Response Code: $respCode")
          println(s"Message Code: $msgCode")
          println(s"Description: $descr")
          println(s"Auth Code: $authCode")

        } else {
          println("Failed Transaction.")
          if (response.getTransactionResponse().getErrors() != null) {

            val errCode = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode()
            val errMsg = response.getTransactionResponse().getErrors().getError().get(0).getErrorText()

            transactionService.save(transaction.copy(
              refundTransaction = Some(RefundTransaction(
                id = UUID.randomUUID(),
                dateTime = clock.instant(),
                responseData = Some(RefundResponseData(
                  transactionID = TransactionID(UUID.randomUUID().toString),
                  responseCode = None,
                  messageCode = None,
                  description = None,
                  authCode = None,
                  errorCode = Some(errCode),
                  errorMessage = Some(errMsg)
                ))
              ))
            ))

            println(s"Error Code: $errCode")
            println(s"Error message: $errMsg")
          }
        }
      } else {
        println("Failed Transaction.")
        if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
          val errCode = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode()
          val errMsg = response.getTransactionResponse().getErrors().getError().get(0).getErrorText()

          transactionService.save(transaction.copy(
            refundTransaction = Some(RefundTransaction(
              id = UUID.randomUUID(),
              dateTime = clock.instant(),
              responseData = Some(RefundResponseData(
                transactionID = TransactionID(UUID.randomUUID().toString),
                responseCode = None,
                messageCode = None,
                description = None,
                authCode = None,
                errorCode = Some(errCode),
                errorMessage = Some(errMsg)
              ))
            ))
          ))

          println(s"Error Code: $errCode")
          println(s"Error message: $errMsg")
        } else {
          val errCode = response.getMessages().getMessage().get(0).getCode()
          val errMsg = response.getMessages().getMessage().get(0).getText()

          transactionService.save(transaction.copy(
            refundTransaction = Some(RefundTransaction(
              id = UUID.randomUUID(),
              dateTime = clock.instant(),
              responseData = Some(RefundResponseData(
                transactionID = TransactionID(UUID.randomUUID().toString),
                responseCode = None,
                messageCode = None,
                description = None,
                authCode = None,
                errorCode = Some(errCode),
                errorMessage = Some(errMsg)
              ))
            ))
          ))

          println(s"Error Code: $errCode")
          println(s"Error message: $errMsg")
        }
      }
    } else {
      println("Null Response.")
    }
    response
  }

  private def voidTransaction(transaction: Transaction): ANetApiResponse = {
    // Create the payment transaction request
    val txnRequest: TransactionRequestType = new TransactionRequestType()
    txnRequest.setTransactionType(TransactionTypeEnum.VOID_TRANSACTION.value())
    txnRequest.setRefTransId(transaction.responseData.get.transactionID.code)
    // Make the API Request
    val apiRequest: CreateTransactionRequest = new CreateTransactionRequest()
    apiRequest.setTransactionRequest(txnRequest)
    val controller: CreateTransactionController = new CreateTransactionController(apiRequest)
    controller.execute()

    val response: CreateTransactionResponse = controller.getApiResponse()

    if(response != null) {
      println("response is not null")
      // If API Response is ok, go ahead and check the transaction response
      if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
        println("ResultCode is OK")
        val result: TransactionResponse = response.getTransactionResponse()
        if (result.getResponseCode().equals("1")) {
          val responseCode = result.getResponseCode()
          val authCode = result.getAuthCode()
          val transId = result.getTransId()

          transactionService.save(transaction.copy(
            voidTransaction = Some(VoidTransaction(
              id = UUID.randomUUID(),
              dateTime = clock.instant(),
              responseCode = Some(responseCode),
              authCode = Some(authCode),
              transactionID = Some(transId),
              resultCode = None
            ))
          ))

          println(s"Response code: $responseCode")
          println("Successfully Voided Transaction")
          println(s"Auth code: $authCode")
          println(s"Transaction ID: $transId")
        } else {
          val respCode = result.getResponseCode()

          transactionService.save(transaction.copy(
            voidTransaction = Some(VoidTransaction(
              id = UUID.randomUUID(),
              dateTime = clock.instant(),
              responseCode = Some(respCode),
              authCode = None,
              transactionID = None,
              resultCode = None
            ))
          ))

          println(s"Failed Transaction: $respCode")
        }
      } else {
        val resultCode = response.getMessages().getResultCode()

        transactionService.save(transaction.copy(
          voidTransaction = Some(VoidTransaction(
            id = UUID.randomUUID(),
            dateTime = clock.instant(),
            responseCode = None,
            authCode = None,
            transactionID = None,
            resultCode = Some(resultCode.toString)
          ))
        ))

        println(s"Failed Transaction: $resultCode")
      }
    }
    response
  }

  private def removeItemFromOrders(
    itemID: String, itemSize: String, itemPrice: Double, order: Order, ordersSeq: Seq[Order], user: User
  )(implicit request: RequestHeader): Future[Unit] = Future {
    order.shoppingBag match {
      case Some(bag) =>
        if (bag.addedItems.exists(a => a.id.toString == itemID)) {
          val addedItem = bag.addedItems.find(a => a.id.toString == itemID).get
          val category = addedItem.category
          itemService.retrieve(addedItem.id).flatMap {
            case Some(item) =>
              if (bag.addedItems.size > 1) {
                println("We have more than one item in the order")
                //
                if (addedItem.size.length >= 1 &&
                  addedItem.size.filter(i => i.number == itemSize).exists(i => i.quantity > 1)) {
                  println("the item with multiple sizes, more than 1 qty of size to remove")

                   itemService.save(item.copy(
                     size = changeSeqSizeToPlus(item.size, itemSize),
                     inventory = item.inventory + 1
                   ))

                   orderService.save(order.copy(
                     shoppingBag = Some(bag.copy(
                       addedItems = bag.addedItems.collect {
                         case i if(i.id.toString == itemID) => i.copy(
                           size = changeSeqSizeToMinus(addedItem.size, itemSize),
                           inventory = addedItem.inventory - 1,
                           total = addedItem.total - addedItem.price
                         )
                         case i if(i.id.toString != itemID) => i
                       },
                       total = bag.total - addedItem.price
                     )),
                     status = "Order Partially Refunded"
                   ))

                   userService.save(user.copy(
                     orders = Some(ordersSeq.collect {
                       case o if(o.id == order.id) => o.copy(
                         shoppingBag = Some(o.shoppingBag.get.copy(
                           addedItems = o.shoppingBag.get.addedItems.collect {
                             case i if(i.id.toString == itemID) => i.copy(
                               size = changeSeqSizeToMinus(addedItem.size, itemSize),
                               inventory = addedItem.inventory - 1,
                               total = addedItem.total - addedItem.price
                             )
                             case i if(i.id.toString != itemID) => i
                           },
                           total = o.shoppingBag.get.total - addedItem.price
                         )),
                         status = "Order Partially Refunded"
                       )
                       case o if(o.id != order.id) => o
                     })
                   )).map { usr =>
                     Ok(ApiResponse(
                       "shopping.item.removed", Messages("shopping.item.removed"),
                       Json.toJson(usr)
                     ))
                   }

                } else if (addedItem.size.length > 1 &&
                  addedItem.size.filter(i => i.number == itemSize).exists(i => i.quantity == 1)) {
                  println("the item with multiple sizes, 1 qty of size to remove")

                  itemService.save(item.copy(
                    size = changeSeqSizeToPlus(item.size, itemSize),
                    inventory = item.inventory + 1
                  ))

                  orderService.save(order.copy(
                    shoppingBag = Some(bag.copy(
                      addedItems = bag.addedItems.collect {
                        case a if(a.id.toString == itemID) => a.copy(
                          size = addedItem.size.filter(i => i.number != itemSize),
                          inventory = addedItem.inventory - 1,
                          total = addedItem.total - addedItem.price
                        )
                        case a if(a.id.toString != itemID) => a
                      },
                      total = bag.total - addedItem.price
                    )),
                    status = "Order Partially Refunded"
                  ))

                  userService.save(user.copy(
                    orders = Some(ordersSeq.collect {
                      case o if(o.id == order.id) => o.copy(
                        shoppingBag = Some(o.shoppingBag.get.copy(
                          addedItems = o.shoppingBag.get.addedItems.collect {
                            case a if(a.id.toString == itemID) => a.copy(
                              size = addedItem.size.filter(i => i.number != itemSize),
                              inventory = addedItem.inventory - 1,
                              total = addedItem.total - addedItem.price
                            )
                            case a if(a.id.toString != itemID) => a
                          },
                          total = o.shoppingBag.get.total - addedItem.price
                        )),
                        status = "Order Partially Refunded"
                      )
                      case o if(o.id != order.id) => o
                    })
                  )).map { usr =>
                    Ok(ApiResponse("shopping.item.removed", Messages("shopping.item.removed"),
                      Json.toJson(usr)))
                  }

                } else if (addedItem.size.length == 1 &&
                  addedItem.size.filter(i => i.number == itemSize).exists(i => i.quantity == 1)) {
                  println("There is more than one item in the order")
                  println("the item with one size, 1 qty of size to remove")
                  val i = order.shoppingBag.get.addedItems.indexOf(addedItem)

                  itemService.save(item.copy(
                    size = changeSeqSizeToPlus(item.size, itemSize),
                    inventory = item.inventory + 1
                  ))

                  orderService.save(order.copy(status = "Order Returned"))

                  userService.save(user.copy(
                    orders = Some(ordersSeq.collect {
                      case o if(o.id == order.id) => o.copy(
                        shoppingBag = Some(o.shoppingBag.get.copy(
                          addedItems = removeElementFromSeq(o.shoppingBag.get.addedItems, i),
                          total = o.shoppingBag.get.total - addedItem.total
                        ))
                      )
                      case o if(o.id != order.id) => o
                    })
                  )).map {
                    savedItem =>
                      Ok(ApiResponse("shopping.item.removed", Messages("shopping.item.removed"),
                        Json.toJson(savedItem)))
                  }

                } else {
                  Future.successful(
                    BadRequest(ApiResponse(
                      "shopping.item.not.available",
                      Messages("shopping.item.not.available")))
                  )
                }
                //
              } else if (bag.addedItems.size == 1) {
                println("We have only one item in the order")
                //
                if (addedItem.size.length >= 1 &&
                  addedItem.size.filter(i => i.number == itemSize).exists(i => i.quantity > 1)) {
                  println("the item with multiple sizes, more than 1 qty of size to remove")

                  itemService.save(item.copy(
                    size = changeSeqSizeToPlus(item.size, itemSize),
                    inventory = item.inventory + 1
                  ))

                  orderService.save(order.copy(
                    shoppingBag = Some(bag.copy(
                      addedItems = bag.addedItems.collect {
                        case a if(a.id.toString == itemID) => a.copy(
                          size = changeSeqSizeToMinus(addedItem.size, itemSize),
                          inventory = addedItem.inventory - 1,
                          total = addedItem.total - addedItem.price
                        )
                        case a if(a.id.toString != itemID) => a
                      },
                      total = bag.total - addedItem.price
                    )),
                    status = "Order Partially Refunded"
                  ))

                  userService.save(user.copy(
                    orders = Some(ordersSeq.collect {
                      case o if(o.id == order.id) => o.copy(
                        shoppingBag = Some(o.shoppingBag.get.copy(
                          addedItems = o.shoppingBag.get.addedItems.collect {
                            case a if(a.id.toString == itemID) => a.copy(
                              size = changeSeqSizeToMinus(addedItem.size, itemSize),
                              inventory = addedItem.inventory - 1,
                              total = addedItem.total - addedItem.price
                            )
                            case a if(a.id.toString != itemID) => a
                          },
                          total = bag.total - addedItem.price
                        )),
                        status = "Order Partially Refunded"
                      )
                      case o if(o.id != order.id) => o
                    })
                  )).map { usr =>
                    Ok(ApiResponse(
                      "shopping.item.removed", Messages("shopping.item.removed"),
                      Json.toJson(usr)))
                  }

                } else if (addedItem.size.length > 1 &&
                  addedItem.size.filter(i => i.number == itemSize).exists(i => i.quantity == 1)) {
                  println("the item with multiple sizes, 1 qty of size to remove")

                  itemService.save(item.copy(
                    size = changeSeqSizeToPlus(item.size, itemSize),
                    inventory = item.inventory + 1
                  ))

                  orderService.save(order.copy(
                    shoppingBag = Some(bag.copy(
                      addedItems = bag.addedItems.collect {
                        case a if(a.id.toString == itemID) => a.copy(
                          size = addedItem.size.filter(i => i.number != itemSize),
                          inventory = addedItem.inventory - 1,
                          total = addedItem.total - addedItem.price
                        )
                        case a if(a.id.toString != itemID) => a
                      },
                      total = bag.total - addedItem.price
                    )),
                    status = "Order Partially Refunded"
                  ))

                  userService.save(user.copy(
                    orders = Some(ordersSeq.collect {
                      case o if(o.id == order.id) => o.copy(
                        shoppingBag = Some(o.shoppingBag.get.copy(
                          addedItems = o.shoppingBag.get.addedItems.collect {
                            case a if(a.id.toString == itemID) => a.copy(
                              size = addedItem.size.filter(i => i.number != itemSize),
                              inventory = addedItem.inventory - 1,
                              total = addedItem.total - addedItem.price
                            )
                            case a if(a.id.toString != itemID) => a
                          },
                          total = o.shoppingBag.get.total - addedItem.price
                        )),
                        status = "Order Partially Refunded"
                      )
                      case o if(o.id != order.id) => o
                    })
                  )).map { usr =>
                    Ok(ApiResponse(
                      "shopping.item.removed", Messages("shopping.item.removed"),
                      Json.toJson(usr)))
                  }

                } else if (addedItem.size.length == 1 &&
                  addedItem.size.filter(i => i.number == itemSize).exists(i => i.quantity == 1)) {
                  println("the item with one size, 1 qty of size to remove")

                  itemService.save(item.copy(
                    size = changeSeqSizeToPlus(item.size, itemSize),
                    inventory = item.inventory + 1
                  ))

                  orderService.save(order.copy(status = "Order Returned"))

                  userService.save(user.copy(
                    orders = Some(ordersSeq.filter(o => o.id != order.id))
                  )).map { usr =>
                    Ok(ApiResponse(
                      "shopping.item.removed", Messages("shopping.item.removed"),
                      Json.toJson(usr)))
                  }

                } else {
                  Future.successful(
                    BadRequest(ApiResponse(
                      "shopping.item.not.available",
                      Messages("shopping.item.not.available")))
                  )
                }
                //
              } else {
                Future.successful(BadRequest(ApiResponse(
                  "shopping.item.not.available", Messages("shopping.item.not.available")
                )))
              }
            case None => Future.successful(BadRequest(ApiResponse(
              "shopping.item.not.available", Messages("shopping.item.not.available")
            )))
          }
        } else {
          Future.successful(BadRequest(ApiResponse(
            "shopping.item.not.available", Messages("shopping.item.not.available")
          )))
        }
      case None => Future.successful(BadRequest(ApiResponse(
        "shopping.item.not.available", Messages("shopping.item.not.available")
      )))
    }
  }

}
