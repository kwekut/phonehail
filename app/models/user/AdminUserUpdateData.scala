package models.user

case class AdminUserUpdateData(
  username: String,
  roles: String,
  email: String,
  phone: String,
  street: String,
  city: String,
  state: String,
  zip: String,
  fullName: String,
  hasstripe: String,
  preferences: String
)