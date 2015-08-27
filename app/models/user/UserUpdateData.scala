package models.user

case class UserUpdateData(
  username: String,
  phone: String,
  address: String,
  fullName: String
)