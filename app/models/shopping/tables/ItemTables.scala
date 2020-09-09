package models.shopping.tables

import java.util.UUID
import org.joda.time.DateTime
import play.api.libs.json.JsValue

case class DbItem(
  id: UUID,
  name: String,
  description: String,
  details: List[String],
  composition: JsValue,
  color: JsValue,
  size: JsValue,
  inventory: Int,
  price: Float,
  currency: String,
  nameOfImg: Int,
  category: String,
  subCategory: String,
  stateOfProduct: String,
  department: String,
  typeOfCollection: String,
  links: List[String],
  availability: String,
  shippingCosts: Float,
  total: Float
)
