package models.core.tables

import scala.collection.immutable.List

case class DbUser(
  userID: String,
  title: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  accountStatus: Option[String],
  dateOfBirth: Option[String]
)

case class DbLoginInfo(
  userId: String,
  providerId: String,
  providerKey: String
)
