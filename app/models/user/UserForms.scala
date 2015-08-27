package models.user

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data._
import play.api.data.Forms._

object UserForms {
  val signInForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )

  val registrationForm = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(RegistrationData.apply)(RegistrationData.unapply)
  )

  val userUpdateForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "address" -> nonEmptyText,
      "fullName" -> nonEmptyText
    )(UserUpdateData.apply)(UserUpdateData.unapply)
  )
  
  val adminSearchForm = Form[String] (
  "filter" -> text
  )

  val adminUserUpdateForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "roles" -> nonEmptyText,
      "email" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "address" -> nonEmptyText,
      "fullName" -> nonEmptyText,
      "hasstripe" -> nonEmptyText,
      "preferences" -> nonEmptyText
    )(AdminUserUpdateData.apply)(AdminUserUpdateData.unapply)
  )

  val stripeForm = Form(
    mapping(
      "cardname" -> text,
      "address1" -> text,
      "address2" -> text,
      "city" -> text,
      "state" -> text,
      "zip" -> text,
      "country" -> text,
      "number" -> text,
      "cvc" -> text,
      "exp-month" -> text,
      "exp-year" -> text,
      "stripeToken" -> nonEmptyText
    )(StripeData.apply)(StripeData.unapply)
  )



  val tokenForm = Form(
    mapping(
      "stripeToken" -> nonEmptyText
    )(TokenData.apply)(TokenData.unapply)
  )

  val imageForm = Form(
    mapping(
      "image" -> nonEmptyText
    )(ImageData.apply)(ImageData.unapply)
  )
}
