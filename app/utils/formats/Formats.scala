package utils.formats

import java.time.Instant
import java.util.UUID

import org.joda.time._

import models.core.{
  AccountData,
  AddNewAddress,
  AddNewAddressData,
  EditAddressData,
  AgeLimit,
  LoginEmail,
  UserAddressesList,
  UserCreditCardsList,
  UserLoginAttempts,
  UserLoginAttemptsList,
  UserWishlist,
  UserOrders,
  UserShoppingBag,
  UserLastItemAlertList,
  LastItemAlert,
  LoginAttempt,
  StateOrProvince,
  PreferredCreditCard,
  AdditionalInfo,
  Address,
  BillingAddress,
  BillingAddressMark,
  CreditCard,
  CardNumberEncrypted,
  DefaultShippingAddressMark,
  PasswordSurvey,
  Registration,
  Settings,
  User,
  UserID,
  NewsletterFashion,
  NewsletterFineJewelry,
  NewsletterHomeCollection,
  Newsletter,
  SubscribeToNewsletter,
  SubscribeToNewsletter2,
  TelephoneEvening,
  TelephoneDay,
  SignIn,
  SignInToShop,
  CompleteSignIn,
  SignUpToShop,
  SignUpEmail,
  CreateAccount,
  PasswordData,
  Color,
  Item,
  ItemComposition,
  ItemSize,
  Size,
  Composition,
  CountryAtPurchase,
  ShoppingBag,
  Order,
  Transaction,
  CardData,
  ResponseData,
  RefundTransaction,
  VoidTransaction,
  RefundResponseData,
  TransactionID,
  ResponseCode,
  AuthCode,
  AVSResultCode,
  CVVResultCode,
  CAVVResultCode,
  TransHashSha2,
  CheckoutData1,
  CheckoutData2,
  CheckoutData3,
  CheckoutData4,
  Checkout,
  PaymentData,
  PaymentData2,
  ShippingAddressAndPaymentDetails,
  AddItemGuest,
  EditItemSizesGuest,
  EditItemQtyGuest,
  RemoveItemGuest,
  WishItem,
  WishItem2,
  OrderNumber,
  OrderToReturn,
  OrderToReturn2,
  ReturnData,
  AddItem,
  EditItemSizes,
  EditItemQty,
  RemoveItem
}
import models.newsletter.NewsletterTask
import play.api.i18n.Lang
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import play.api.libs.functional.syntax._
import scala.util.{ Failure, Success, Try }

/**
 * Implicit JSON formats.
 */
trait Formats {

  /**
   * Renames a branch if it exists.
   */
  val RenameBranch: (JsPath, JsPath) => Reads[JsObject] = (oldPath: JsPath, newPath: JsPath) => {
    (__.json.update(newPath.json.copyFrom(oldPath.json.pick)) andThen oldPath.json.prune).orElse(__.json.pick[JsObject])
  }

  /**
   * Renames the field "_id" into the value given as `name` parameter.
   */
  val IDReads: String => Reads[JsObject] = (name: String) => RenameBranch(__ \ '_id, __ \ Symbol(name))

  /**
   * Transforms the field with the given `name` into the "_id" field.
   */
  val IDWrites: String => JsValue => JsObject = (name: String) => (js: JsValue) => {
    js.as[JsObject] - name ++ (js \ name match {
      case JsDefined(v) => Json.obj("_id" -> v)
      case _            => Json.obj()
    })
  }

  /**
   * Converts [[play.api.i18n.Lang]] object to JSON and vice versa.
   */
  implicit object LangFormat extends Format[Lang] {
    def reads(json: JsValue): JsResult[Lang] = JsSuccess(Lang(json.as[String]))
    def writes(o: Lang): JsValue = JsString(o.code)
  }

  /**
   * Converts [[java.util.UUID]] object to JSON and vice versa.
   */
  implicit object UUIDFormat extends Format[UUID] {
    def reads(json: JsValue): JsResult[UUID] = Try(UUID.fromString(json.as[String])) match {
      case Success(id) => JsSuccess(id)
      case Failure(e)  => JsError(e.getMessage)
    }
    def writes(o: UUID): JsValue = JsString(o.toString)
  }

  /**
   * Converts [[java.time.Instant]] object to JSON and vice versa.
   */
  implicit object InstantFormat extends Format[Instant] {
    def reads(json: JsValue): JsResult[Instant] =
      (__ \ "$date").read[Long].map(Instant.ofEpochMilli).reads(json)
    def writes(o: Instant): JsValue = Json.obj(
      "$date" -> o.toEpochMilli
    )
  }

  /**
   * Converts a [[CountryAtPurchase]] instance to JSON and vice versa.
   */
  implicit val countryAtPurchaseFormat: OFormat[CountryAtPurchase] = Json.format

  /**
   * Converts a [[Composition]] instance to JSON and vice versa.
   */
  implicit val compositionFormat: OFormat[Composition] = Json.format

  /**
   * Converts a [[ItemComposition]] instance to JSON and vice versa.
   */
  implicit val itemCompositionFormat: OFormat[ItemComposition] = Json.format

  /**
   * Converts a [[Size]] instance to JSON and vice versa.
   */
  implicit val sizeFormat: OFormat[Size] = Json.format

  /**
   * Converts a [[ItemSize]] instance to JSON and vice versa.
   */
  implicit val itemSizeFormat: OFormat[ItemSize] = Json.format

  /**
   * Converts a [[Color]] instance to JSON and vice versa.
   */
  implicit val colorFormat: OFormat[Color] = Json.format

  /**
   * Converts a [[Item]] instance to JSON and vice versa.
   */
  implicit val itemFormat: OFormat[Item] = Json.format

  /**
   * Converts a [[AddItem]] instance to JSON and vice versa.
   */
  implicit val addItemFormat: OFormat[AddItem] = Json.format

  /**
    * Converts JSON into a [[EditItemSizes]] instance.
    */
   implicit val editItemSizesReads: Reads[EditItemSizes] = IDReads("itemID") andThen Json.reads

   /**
    * Converts a [[EditItemSizes]] instance to JSON.
    */
   implicit val editItemSizesWrites: OWrites[EditItemSizes] = Json.writes.transform(IDWrites("itemID"))

  /**
    * Converts JSON into a [[EditItemQty]] instance.
    */
   implicit val editItemQtyReads: Reads[EditItemQty] = IDReads("itemID") andThen Json.reads

   /**
    * Converts a [[EditItemQty]] instance to JSON.
    */
   implicit val editItemQtyWrites: OWrites[EditItemQty] = Json.writes.transform(IDWrites("itemID"))

  /**
    * Converts JSON into a [[RemoveItem]] instance.
    */
   implicit val removeItemReads: Reads[RemoveItem] = IDReads("itemID") andThen Json.reads

   /**
    * Converts a [[RemoveItem]] instance to JSON.
    */
   implicit val removeItemWrites: OWrites[RemoveItem] = Json.writes.transform(IDWrites("itemID"))

  /**
   * Converts a [[WishItem]] instance to JSON and vice versa.
   */
  implicit val wishItemFormat: OFormat[WishItem] = Json.format

  /**
    * Converts JSON into a [[WishItem2]] instance.
    */
   implicit val wishItem2Reads: Reads[WishItem2] = IDReads("productID") andThen Json.reads

   /**
    * Converts a [[WishItem2]] instance to JSON.
    */
   implicit val wishItem2Writes: OWrites[WishItem2] = Json.writes.transform(IDWrites("productID"))

   /**
    * Converts a [[WishItem2]] instance to JSON and vice versa.
    */
   implicit val wishItem2Format: OFormat[WishItem2] = Json.format

   /**
    * Converts a [[ReturnData]] instance to JSON and vice versa.
    */
   implicit val returnDataFormat: OFormat[ReturnData] = Json.format

   /**
     * Converts JSON into a [[OrderToReturn]] instance.
     */
    implicit val orderToReturnReads: Reads[OrderToReturn] = IDReads("id") andThen Json.reads

    /**
     * Converts a [[OrderToReturn]] instance to JSON.
     */
    implicit val orderToReturnWrites: OWrites[OrderToReturn] = Json.writes.transform(IDWrites("id"))

    /**
      * Converts JSON into a [[OrderToReturn2]] instance.
      */
     implicit val orderToReturn2Reads: Reads[OrderToReturn2] = IDReads("id") andThen Json.reads

     /**
      * Converts a [[OrderToReturn2]] instance to JSON.
      */
     implicit val orderToReturn2Writes: OWrites[OrderToReturn2] = Json.writes.transform(IDWrites("id"))

    /**
      * Converts JSON into a [[OrderNumber]] instance.
      */
     implicit val orderNumberReads: Reads[OrderNumber] = IDReads("orderNumber") andThen Json.reads

     /**
      * Converts a [[OrderNumber]] instance to JSON.
      */
     implicit val orderNumberWrites: OWrites[OrderNumber] = Json.writes.transform(IDWrites("orderNumber"))

    /**
     * Converts a [[OrderToReturn]] instance to JSON and vice versa.
     */
    implicit val orderToReturnFormat: OFormat[OrderToReturn] = Json.format

  /**
    * Converts JSON into a [[ShoppingBag]] instance.
    */
   implicit val shoppingBagReads: Reads[ShoppingBag] = IDReads("id") andThen Json.reads

   /**
    * Converts a [[ShoppingBag]] instance to JSON.
    */
   implicit val shoppingBagWrites: OWrites[ShoppingBag] = Json.writes.transform(IDWrites("id"))

   /**
    * Converts a [[ShoppingBag]] instance to JSON and vice versa.
    */
   implicit val shoppingBagFormat: OFormat[ShoppingBag] = Json.format

   /**
    * Converts a [[SignIn]] instance to JSON and vice versa.
    */
   implicit val signInFormat: OFormat[SignIn] = Json.format

   /**
    * Converts a [[SignInToShop]] instance to JSON and vice versa.
    */
   implicit val singInToShopFormat: OFormat[SignInToShop] = Json.format

   /**
    * Converts a [[PasswordData]] instance to JSON and vice versa.
    */
   implicit val passwordDataFormat: OFormat[PasswordData] = Json.format

   /**
    * Converts a [[CompleteSignIn]] instance to JSON and vice versa.
    */
   implicit val completeSignInFormat: OFormat[CompleteSignIn] = Json.format

   /**
    * Converts a [[SignUpEmail]] instance to JSON and vice versa.
    */
   implicit val signUpEmailFormat: OFormat[SignUpEmail] = Json.format

   /**
    * Converts a [[AccountData]] instance to JSON and vice versa.
    */
   implicit val accountDataFormat: OFormat[AccountData] = Json.format

   /**
    * Converts a [[CreateAccount]] instance to JSON and vice versa.
    */
   implicit val createAccountFormat: OFormat[CreateAccount] = Json.format

   /**
    * Converts a [[SignUpToShop]] instance to JSON and vice versa.
    */
   implicit val signUpToShopFormat: OFormat[SignUpToShop] = Json.format

   /**
    * Converts a [[AddItemGuest]] instance to JSON and vice versa.
    */
   implicit val addItemGuestFormat: OFormat[AddItemGuest] = Json.format

   /**
     * Converts JSON into a [[EditItemSizesGuest]] instance.
     */
    implicit val editItemSizesGuestReads: Reads[EditItemSizesGuest] = IDReads("itemID") andThen Json.reads

    /**
     * Converts a [[EditItemSizesGuest]] instance to JSON.
     */
    implicit val editItemSizesGuestWrites: OWrites[EditItemSizesGuest] = Json.writes.transform(IDWrites("itemID"))

    /**
     * Converts a [[EditItemSizesGuest]] instance to JSON and vice versa.
     */
    implicit val editItemSizesGuestFormat: OFormat[EditItemSizesGuest] = Json.format

   /**
     * Converts JSON into a [[EditItemQtyGuest]] instance.
     */
    implicit val editItemQtyGuestReads: Reads[EditItemQtyGuest] = IDReads("itemID") andThen Json.reads

    /**
     * Converts a [[EditItemQtyGuest]] instance to JSON.
     */
    implicit val editItemQtyGuestWrites: OWrites[EditItemQtyGuest] = Json.writes.transform(IDWrites("itemID"))

    /**
     * Converts a [[EditItemQtyGuest]] instance to JSON and vice versa.
     */
    implicit val editItemQtyGuestFormat: OFormat[EditItemQtyGuest] = Json.format

   /**
     * Converts JSON into a [[RemoveItemGuest]] instance.
     */
    implicit val removeItemGuestReads: Reads[RemoveItemGuest] = IDReads("itemID") andThen Json.reads

    /**
     * Converts a [[RemoveItemGuest]] instance to JSON.
     */
    implicit val removeItemGuestWrites: OWrites[RemoveItemGuest] = Json.writes.transform(IDWrites("itemID"))

    /**
     * Converts a [[RemoveItemGuest]] instance to JSON and vice versa.
     */
    implicit val removeItemGuestFormat: OFormat[RemoveItemGuest] = Json.format

   /**
     * Converts JSON into a [[Order]] instance.
     */
    implicit val orderReads: Reads[Order] = IDReads("id") andThen Json.reads

    /**
     * Converts a [[Order]] instance to JSON.
     */
    implicit val orderWrites: OWrites[Order] = Json.writes.transform(IDWrites("id"))

   /**
    * Converts a [[Order]] instance to JSON and vice versa.
    */
   implicit val orderFormat: OFormat[Order] = Json.format

   /**
    * Converts a [[UserShoppingBag]] instance to JSON and vice versa.
    */
   implicit val userShoppingBagFormat: OFormat[UserShoppingBag] = Json.format

   /**
    * Converts a [[LastItemAlert]] instance to JSON and vice versa.
    */
   implicit val lastItemAlertFormat: OFormat[LastItemAlert] = Json.format

   /**
    * Converts a [[UserLastItemAlertList]] instance to JSON and vice versa.
    */
   implicit val UserLastItemAlertListFormat: OFormat[UserLastItemAlertList] = Json.format

   /**
    * Converts a [[UserWishlist]] instance to JSON and vice versa.
    */
   implicit val userWishlistFormat: OFormat[UserWishlist] = Json.format

   /**
    * Converts a [[UserOrders]] instance to JSON and vice versa.
    */
   implicit val userOrdersFormat: OFormat[UserOrders] = Json.format

  /**
   * Converts a [[AddNewAddress]] instance to JSON and vice versa.
   */
  implicit val addNewAddressFormat: OFormat[AddNewAddress] = Json.format

  /**
   * Converts a [[EditAddressData]] instance to JSON and vice versa.
   */
  implicit val editAddressFormat: OFormat[EditAddressData] = Json.format

  /**
   * Converts a [[AddNewAddressData]] instance to JSON and vice versa.
   */
  implicit val addNewAddressDataFormat: OFormat[AddNewAddressData] = Json.format

  /**
   * Converts a [[StateOrProvince]] instance to JSON and vice versa.
   */
  implicit val stateOrProvinceFormat: OFormat[StateOrProvince] = Json.format

  /**
   * Converts a [[PreferredCreditCard]] instance to JSON and vice versa.
   */
  implicit val prefCreditCardFormat: OFormat[PreferredCreditCard] = Json.format

  /**
   * Converts a [[BillingAddress]] instance to JSON and vice versa.
   */
  implicit val billingAddressFormat: OFormat[BillingAddress] = Json.format

  /**
   * Converts a [[CardNumberEncrypted]] instance to JSON and vice versa.
   */
  implicit val cardNumEncryptedFormat: OFormat[CardNumberEncrypted] = Json.format

  /**
   * Converts a [[BillingAddressMark]] instance to JSON and vice versa.
   */
  implicit val billingAddrMarkFormat: OFormat[BillingAddressMark] = Json.format

  /**
   * Converts a [[DefaultShippingAddressMark]] instance to JSON and vice versa.
   */
  implicit val defShipAddrMarkFormat: OFormat[DefaultShippingAddressMark] = Json.format

  /**
   * Converts a [[TelephoneEvening]] instance to JSON and vice versa.
   */
  implicit val telephoneEveningFormat: OFormat[TelephoneEvening] = Json.format

  /**
   * Converts a [[TelephoneDay]] instance to JSON and vice versa.
   */
  implicit val telephoneDayFormat: OFormat[TelephoneDay] = Json.format

  /**
   * Converts a [[AdditionalInfo]] instance to JSON and vice versa.
   */
  implicit val additionalInfoFormat: OFormat[AdditionalInfo] = Json.format

  /**
   * Converts a [[CreditCard]] instance to JSON and vice versa.
   */
  implicit val creditCardFormat: OFormat[CreditCard] = Json.format

  /**
   * Converts a [[Address]] instance to JSON and vice versa.
   */
  implicit val addressFormat: OFormat[Address] = Json.format

  /**
   * Converts a [[UserCreditCardsList]] instance to JSON and vice versa.
   */
  implicit val userCreditCardsListFormat: OFormat[UserCreditCardsList] = Json.format

  /**
   * Converts a [[UserAddressesList]] instance to JSON and vice versa.
   */
  implicit val userAddressesListFormat: OFormat[UserAddressesList] = Json.format

  /**
   * Converts a [[Checkout]] instance to JSON and vice versa.
   */
  implicit val checkoutFormat: OFormat[Checkout] = Json.format

  /**
   * Converts a [[PaymentData]] instance to JSON and vice versa.
   */
  implicit val paymentDataFormat: OFormat[PaymentData] = Json.format

  /**
   * Converts a [[PaymentData2]] instance to JSON and vice versa.
   */
  implicit val paymentData2Format: OFormat[PaymentData2] = Json.format

  /**
   * Converts a [[ShippingAddressAndPaymentDetails]] instance to JSON and vice versa.
   */
  implicit val shipAddrPaymDetFormat: OFormat[ShippingAddressAndPaymentDetails] = Json.format

  /**
   * Converts a [[CheckoutData1]] instance to JSON and vice versa.
   */
  implicit val checkoutData1Format: OFormat[CheckoutData1] = Json.format

  /**
   * Converts a [[CheckoutData2]] instance to JSON and vice versa.
   */
  implicit val checkoutData2Format: OFormat[CheckoutData2] = Json.format

  /**
   * Converts a [[CheckoutData3]] instance to JSON and vice versa.
   */
  implicit val checkoutData3Format: OFormat[CheckoutData3] = Json.format

  /**
   * Converts a [[CheckoutData4]] instance to JSON and vice versa.
   */
  implicit val checkoutData4Format: OFormat[CheckoutData4] = Json.format

  /**
   * Converts JSON into a [[NewsletterTask]] instance.
   */
  implicit val newsletterTaskReads: Reads[NewsletterTask] = IDReads("id") andThen Json.reads

  /**
   * Converts a [[NewsletterTask]] instance to JSON.
   */
  implicit val newsletterTaskWrites: OWrites[NewsletterTask] = Json.writes.transform(IDWrites("id"))

  /**
   * Converts a [[NewsletterTask]] instance to JSON and vice versa.
   */
  implicit val newsletterTaskFormat: OFormat[NewsletterTask] = Json.format

  /**
   * Converts a [[NewsletterHomeCollection]] instance to JSON and vice versa.
   */
  implicit val newsletterHomeCollectionFormat: OFormat[NewsletterHomeCollection] = Json.format

  /**
   * Converts a [[NewsletterFineJewelry]] instance to JSON and vice versa.
   */
  implicit val newsletterFineJewelryFormat: OFormat[NewsletterFineJewelry] = Json.format

  /**
   * Converts a [[NewsletterFashion]] instance to JSON and vice versa.
   */
  implicit val newsletterFashionFormat: OFormat[NewsletterFashion] = Json.format

  /**
   * Converts a [[Newsletter]] instance to JSON and vice versa.
   */
  implicit val newsletterFormat: OFormat[Newsletter] = Json.format

  /**
   * Converts JSON into a [[SubscribeToNewsletter]] instance.
   */
  implicit val subscribeToNewsletterReads: Reads[SubscribeToNewsletter] = IDReads("id") andThen Json.reads

  /**
   * Converts a [[SubscribeToNewsletter]] instance to JSON.
   */
  implicit val subscribeToNewsletterWrites: OWrites[SubscribeToNewsletter] = Json.writes.transform(IDWrites("id"))

  /**
   * Converts a [[SubscribeToNewsletter]] instance to JSON and vice versa.
   */
  implicit val subscribeToNewsletterFormat: OFormat[SubscribeToNewsletter] = Json.format

  /**
   * Converts a [[SubscribeToNewsletter2]] instance to JSON and vice versa.
   */
  implicit val subscribeToNewsletter2Format: OFormat[SubscribeToNewsletter2] = Json.format

   /**
    * Converts a [[LoginAttempt]] instance to JSON and vice versa.
    */
   implicit val loginAttemptFormat: OFormat[LoginAttempt] = Json.format

   /**
    * Converts JSON into a [[LoginAttempt]] instance.
    */
   implicit val loginAttemptReads: Reads[LoginAttempt] = Json.reads

   /**
    * Converts a [[LoginAttempt]] instance to JSON.
    */
   implicit val loginAttemptWrites: OWrites[LoginAttempt] = Json.writes

   /**
    * Converts JSON into a [[UserLoginAttempts]] instance.
    */
   implicit val userLoginAttemptsReads: Reads[UserLoginAttempts] = IDReads("id") andThen Json.reads

   /**
    * Converts a [[UserLoginAttempts]] instance to JSON.
    */
   implicit val userLoginAttemptsWrites: OWrites[UserLoginAttempts] = Json.writes.transform(IDWrites("id"))

   /**
    * Converts a [[UserLoginAttempts]] instance to JSON and vice versa.
    */
   implicit val userLoginAttemptsFormat: OFormat[UserLoginAttempts] = Json.format

   /**
    * Converts a [[UserLoginAttemptsList]] instance to JSON and vice versa.
    */
   implicit val userLoginAttemptsListFormat: OFormat[UserLoginAttemptsList] = Json.format

  /**
   * Converts a [[PasswordSurvey]] instance to JSON and vice versa.
   */
  implicit val passwordSurveyFormat: OFormat[PasswordSurvey] = Json.format

  /**
   * Converts a [[AgeLimit]] instance to JSON and vice versa.
   */
  implicit val ageLimitFormat: OFormat[AgeLimit] = Json.format

  /**
   * Converts a [[Registration]] instance to JSON and vice versa.
   */
  implicit val registrationFormat: OFormat[Registration] = Json.format

  /**
   * Converts a [[Settings]] instance to JSON and vice versa.
   */
  implicit val settingsFormat: OFormat[Settings] = Json.format

 /**
   * Converts JSON into a [[User]] instance.
   */
  implicit val userReads: Reads[User] = IDReads("userID") andThen Json.reads

  /**
   * Converts a [[User]] instance to JSON.
   */
  implicit val userWrites: OWrites[User] = Json.writes.transform(IDWrites("userID"))

  /**
   * Converts a [[User]] instance to JSON and vice versa.
   */
  implicit val userFormat: OFormat[User] = Json.format

  /**
   * Converts a [[CardData]] instance to JSON and vice versa.
   */
  implicit val cardDataFormat: OFormat[CardData] = Json.format

  /**
   * Converts a [[TransHashSha2]] instance to JSON and vice versa.
   */
  implicit val transHashSha2Format: OFormat[TransHashSha2] = Json.format

  /**
   * Converts a [[CAVVResultCode]] instance to JSON and vice versa.
   */
  implicit val cavvResultCodeFormat: OFormat[CAVVResultCode] = Json.format

  /**
   * Converts a [[CVVResultCode]] instance to JSON and vice versa.
   */
  implicit val cvvResultCodeFormat: OFormat[CVVResultCode] = Json.format

  /**
   * Converts a [[AVSResultCode]] instance to JSON and vice versa.
   */
  implicit val avsResultCodeFormat: OFormat[AVSResultCode] = Json.format

  /**
   * Converts a [[AuthCode]] instance to JSON and vice versa.
   */
  implicit val authCodeFormat: OFormat[AuthCode] = Json.format

  /**
   * Converts a [[ResponseCode]] instance to JSON and vice versa.
   */
  implicit val responseCodeFormat: OFormat[ResponseCode] = Json.format

  /**
   * Converts a [[TransactionID]] instance to JSON and vice versa.
   */
  implicit val transactionIDFormat: OFormat[TransactionID] = Json.format

  /**
   * Converts a [[RefundResponseData]] instance to JSON and vice versa.
   */
  implicit val refundResponseDataFormat: OFormat[RefundResponseData] = Json.format

  /**
    * Converts JSON into a [[VoidTransaction]] instance.
    */
   implicit val voidTransactionReads: Reads[VoidTransaction] = IDReads("id") andThen Json.reads

   /**
    * Converts a [[VoidTransaction]] instance to JSON.
    */
   implicit val voidTransactionWrites: OWrites[VoidTransaction] = Json.writes.transform(IDWrites("id"))

  /**
   * Converts a [[VoidTransaction]] instance to JSON and vice versa.
   */
  implicit val voidTransactionFormat: OFormat[VoidTransaction] = Json.format

  /**
    * Converts JSON into a [[RefundTransaction]] instance.
    */
   implicit val refundTransactionReads: Reads[RefundTransaction] = IDReads("id") andThen Json.reads

   /**
    * Converts a [[RefundTransaction]] instance to JSON.
    */
   implicit val refundTransactionWrites: OWrites[RefundTransaction] = Json.writes.transform(IDWrites("id"))

  /**
   * Converts a [[RefundTransaction]] instance to JSON and vice versa.
   */
  implicit val refundTransactionFormat: OFormat[RefundTransaction] = Json.format

  /**
   * Converts a [[ResponseData]] instance to JSON and vice versa.
   */
  implicit val responseDataFormat: OFormat[ResponseData] = Json.format

  /**
    * Converts JSON into a [[Transaction]] instance.
    */
   implicit val transactionReads: Reads[Transaction] = IDReads("id") andThen Json.reads

   /**
    * Converts a [[Transaction]] instance to JSON.
    */
   implicit val transactionWrites: OWrites[Transaction] = Json.writes.transform(IDWrites("id"))

  /**
   * Converts a [[Transaction]] instance to JSON and vice versa.
   */
  implicit val transactionFormat: OFormat[Transaction] = Json.format


}

/**
 * Implicit JSON formats.
 */
object Formats extends Formats
