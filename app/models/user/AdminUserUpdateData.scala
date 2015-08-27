package models.user

case class AdminUserUpdateData(
  username: String,
  roles: String,
  email: String,
  phone: String,
  address: String,
  fullName: String,
  hasstripe: String,
  preferences: String
)