package controllers

import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import models.user.{ User, UserForms }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.AnyContent
import play.api.mvc.Action
import scala.util.{Success, Failure}
import scala.concurrent.Future
import java.io.File
import play.api.Logger

@javax.inject.Singleton
class ImageController @javax.inject.Inject() 
        ( val messagesApi: MessagesApi, val env: AuthenticationEnvironment) 
                                                    extends Silhouette[User, CookieAuthenticator] {
  
  def imageForm = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>  Future.successful ( Ok(views.html.image(UserForms.imageForm)) )
      case None => Future.successful ( Redirect(controllers.routes.HomeController.index()).flashing("error" -> "You are not signed in") )
    }
  }

  
  def upload = SecuredAction.async { implicit request =>
    Logger.info("upload controller called")
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(currentUser) => 
      Logger.info("User available") 
        UserForms.imageForm.bindFromRequest.fold(
                form => Future.successful( BadRequest(views.html.image(form)) ),
                data =>     
          env.userService.save(currentUser.copy(image =  Some(data.imageUrl)), update = true).flatMap {
              case user => Future.successful (  Redirect(controllers.routes.ProfileController.userprofile).flashing("error" -> "Image uploaded") )
          }
        )
      case None => Future.successful ( Redirect(controllers.routes.HomeController.index()).flashing("error" -> "You are not signed in") )
    }  
  }

}

