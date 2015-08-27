package controllers

import jdub.async.Database
import models.queries.ProfileQueries
import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.user.{ User, UserForms }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import scala.concurrent.Future

@javax.inject.Singleton
class ProfileController @javax.inject.Inject() (val messagesApi: MessagesApi, val env: AuthenticationEnvironment) extends Silhouette[User, CookieAuthenticator] {
  def profile = SecuredAction.async { implicit request =>
    Database.query(ProfileQueries.FindProfilesByUser(request.identity.id)).map { profiles =>
      Ok(views.html.profile(request.identity, profiles))
    }
  }

  def userprofile = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.userprofile(request.identity)))
  }
}
