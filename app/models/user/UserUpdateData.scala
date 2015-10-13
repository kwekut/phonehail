package models.user

case class UserUpdateData(
  username: String,
  phone: String,
  street: String,
  city: String,
  state: String,
  zip: String,
  fullName: String
)