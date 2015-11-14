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
      "street" -> nonEmptyText,
      "city" -> nonEmptyText,
      "state" -> nonEmptyText,
      "zip" -> nonEmptyText,
      "fullName" -> nonEmptyText
    )(UserUpdateData.apply)(UserUpdateData.unapply)
  )
  
  val adminSearchForm = Form[String] (
  "filter" -> text
  )

  val dashDateSearchForm = Form(
    mapping(
      "startmonth" -> nonEmptyText,
      "startyear" -> nonEmptyText,
      "endmonth" -> nonEmptyText,
      "endyear" -> nonEmptyText
    )(DashDateData.apply)(DashDateData.unapply)
  )

  val dashSearchForm = Form[String] (
  "filter" -> text
  )

  val adminUserUpdateForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "roles" -> nonEmptyText,
      "email" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "street" -> nonEmptyText,
      "city" -> nonEmptyText,
      "state" -> nonEmptyText,
      "zip" -> nonEmptyText,
      "fullName" -> nonEmptyText,
      "hasstripe" -> nonEmptyText,
      "preferences" -> nonEmptyText
    )(AdminUserUpdateData.apply)(AdminUserUpdateData.unapply)
  )

  val adminDashUpdateForm = Form(
    mapping(
      "id" -> nonEmptyText,
      "clientname" -> nonEmptyText,
      "clientemail" -> nonEmptyText,
      "clientphone" -> nonEmptyText,
      "clientzip" -> nonEmptyText,
      "clientcity" -> nonEmptyText,
      "clientstate" -> nonEmptyText,
      "driverphone" -> nonEmptyText,
      "drivername" -> nonEmptyText,
      "drivercompany" -> nonEmptyText,
      "pickuplocation" -> nonEmptyText,
      "attendantnamecomment" -> nonEmptyText,
      "chargedamount" -> nonEmptyText,
      "chargecomment" -> nonEmptyText,
      "other" -> nonEmptyText
    )(AdminDashUpdateData.apply)(AdminDashUpdateData.unapply)
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
