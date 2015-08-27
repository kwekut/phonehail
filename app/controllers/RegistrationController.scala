package controllers

import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import models.user.{ RegistrationData, User, UserForms, UserUpdateData }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.AnyContent
import services.user.AuthenticationEnvironment
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.mvc.Action
import play.api.Logger

import scala.concurrent.Future

@javax.inject.Singleton
class RegistrationController @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val env: AuthenticationEnvironment
) extends Silhouette[User, CookieAuthenticator] {

  def registrationForm = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => //Future.successful {Redirect(controllers.routes.UserUpdateController.userUpdateForm())}
      val info = UserUpdateData(user.username.getOrElse("Enter UserName"), user.phone.getOrElse("Enter Phone Number"), user.address.getOrElse("Enter Address"), user.fullName.getOrElse("Enter FullName"))
      Future.successful { Ok(views.html.userupdate(user, UserForms.userUpdateForm.fill(info))) }
      case None => Future.successful(Ok(views.html.register(UserForms.registrationForm)))
    }
  }

  def register = Action.async { implicit request =>
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.register(form))),
      data => {
        env.identityService.retrieve(LoginInfo(CredentialsProvider.ID, data.email)).flatMap {
          case Some(user) => Future.successful {
            Ok(views.html.register(UserForms.registrationForm.fill(data))).flashing("error" -> "That email address is already taken.")
          }
          case None => env.identityService.retrieve(data.email) flatMap {
            case Some(user) => Future.successful {
              Ok(views.html.register(UserForms.registrationForm.fill(data))).flashing("error" -> "That email is already taken.")
            }
            case None => 
              val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
              val authInfo = env.hasher.hash(data.password)

              val profile = CommonSocialProfile(
                loginInfo = loginInfo,
                email = Some(data.email)
              )
              val r = Redirect(controllers.routes.UserUpdateController.userUpdateForm())
              for {
                avatar <- env.avatarService.retrieveURL(data.email)
                user <- env.userService.create(profile.copy(avatarURL = avatar.orElse(Some("default"))))
                authInfo <- env.authInfoService.save(loginInfo, authInfo)
                authenticator <- env.authenticatorService.create(loginInfo)
                value <- env.authenticatorService.init(authenticator)
                result <- env.authenticatorService.embed(value, r)
              } yield {
                env.eventBus.publish(SignUpEvent(user, request, request2Messages))
                env.eventBus.publish(LoginEvent(user, request, request2Messages))
                result
              }
          }
        }
      }
    )
  }
}
