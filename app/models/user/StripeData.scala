package models.user

case class StripeData(
  cardname: String,
  address1: String,
  address2: String,
  city: String,
  state: String,
  zip: String,
  country: String,
  number: String,
  cvc: String,
  expmonth: String,
  expyear: String,
  stripeToken: String

)





