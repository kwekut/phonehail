package models.user

import com.stripe.model.Token

case class TokenData(
  stripeToken: String
)