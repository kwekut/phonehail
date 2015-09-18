package controllers

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{ LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CommonSocialProfileBuilder, SocialProvider }
import services.user.AuthenticationEnvironment
import models.user.{ User, UserForms, UserUpdateData }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.Logger

import scala.concurrent.Future

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val env: AuthenticationEnvironment
) extends Silhouette[User, CookieAuthenticator] {

  def signInForm = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>  Future.successful { Ok(views.html.userprofile(user)) }
      case None => Future.successful(Ok(views.html.signin(UserForms.signInForm)))
    }
  }

  def authenticateCredentials = Action.async { implicit request =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signin(form))),
      credentials => env.credentials.authenticate(credentials).flatMap { loginInfo =>
        
        env.identityService.retrieve(loginInfo).flatMap {
          case Some(user) => env.authenticatorService.create(loginInfo).flatMap { authenticator =>
            
            val result = Redirect(controllers.routes.UserUpdateController.userUpdateForm())
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            env.authenticatorService.init(authenticator).flatMap(v => env.authenticatorService.embed(v, result))
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user."))
        }
      }.recover {
        case e: ProviderException =>
          Redirect(controllers.routes.AuthenticationController.signInForm()).flashing(("error", "Invalid Credentials."))
      }
    )
  }

  def authenticateSocial(provider: String) = Action.async { implicit request =>
    (env.providersMap.get(provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => 
          val r = Redirect(controllers.routes.UserUpdateController.userUpdateForm())
          for {
            profile <- p.retrieveProfile(authInfo)
            user <- env.userService.create(profile)
            authInfo <- env.authInfoService.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(profile.loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(value, r)
          } yield {
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            result
          }
        }
      case _ => Future.failed(new ProviderException("Invalid provider [" + provider + "]."))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.AuthenticationController.signInForm()).flashing(("error", "Service error with provider [" + provider + "]."))
    }
  }

  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(controllers.routes.HomeController.index())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, result).map(x => result)
  }


  // private[this] def mergeUser(user: User, profile: CommonSocialProfile) = {
  //   user.copy(
  //     username = if (profile.firstName.isDefined && user.username.isEmpty) { profile.firstName } else { user.username }
  //   )
  // }
}
