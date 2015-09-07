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
      "phone" -> nonEmptyText(minLength = 10, maxLength = 10),
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


  val tokenForm = Form(
    mapping(
      "stripeToken" -> nonEmptyText
    )(TokenData.apply)(TokenData.unapply)
  )

  val imageForm = Form(
    mapping(
      "imageUrl" -> nonEmptyText
    )(ImageData.apply)(ImageData.unapply)
  )
}
