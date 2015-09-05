package controllers

import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import models.user.{ User, UserForms }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (val messagesApi: MessagesApi, val env: AuthenticationEnvironment) extends Silhouette[User, CookieAuthenticator] {
  
  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Ok(views.html.index(Some(user))))
      case None => Future.successful(Ok(views.html.index()))
    }
  }
}
