package controllers

import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import models.user.{ User, TokenData, UserForms }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.AnyContent
import services.user.AuthenticationEnvironment
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.mvc.Action
import models.stripe.{ StripeService, StripeImpl } 
import play.api.Logger

import com.stripe.exception.APIConnectionException
import com.stripe.exception.APIException
import com.stripe.exception.AuthenticationException
import com.stripe.exception.CardException
import com.stripe.exception.InvalidRequestException
import com.stripe.exception.StripeException
import com.stripe.model.Customer
import scala.concurrent.Future

@javax.inject.Singleton
class StripeController @javax.inject.Inject() (
    val stripeSer: StripeService,
    val messagesApi: MessagesApi,
    val env: AuthenticationEnvironment
) extends Silhouette[User, CookieAuthenticator] {

  def stripeForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  Future.successful(Ok(views.html.stripe(user, UserForms.tokenForm)))
      case None =>  Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("error" -> "You are not signed in"))
    }
  }

  def createcustomer = SecuredAction.async { implicit request =>
    Logger.info("stripe controller started")
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>
        UserForms.tokenForm.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.stripe(user, form))),
          data => Future.successful(stripeSer.createCustomer(request.identity.id, data.stripeToken)).flatMap {
            case _ => Future.successful(Redirect(controllers.routes.ProfileController.userprofile).flashing("error" -> "You are now ready to start using our service"))
          }.recover {
            case e: CardException =>  Ok(views.html.stripe(user, UserForms.tokenForm)).flashing("error" -> "Incorrect card details")
            case e: InvalidRequestException =>  Ok(views.html.stripe(user, UserForms.tokenForm)).flashing("error" -> "Invalid details entered")
            case e: AuthenticationException =>  Redirect(controllers.routes.HomeController.index())
            //case e: APIConnectionException =>  Redirect(controllers.routes.HomeController.index())
            //case e: StripeException =>  Redirect(controllers.routes.HomeController.index())
            //case e: Exception =>  Redirect(controllers.routes.HomeController.index())
          }
        )
      case None =>  Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("error" -> "Couldn't update payment information"))
    }
  }



  // def createcustomer = SecuredAction.async { implicit request =>
  //   UserForms.stripeForm.bindFromRequest.fold(
  //     form => Future.successful(BadRequest(views.html.stripe(form))),
  //     data => env.stripeService.createCustomer(request.identity.id, data).flatMap {
  //       case Some(customer) =>  Redirect(controllers.routes.HomeController.index())
  //       case None => Logger.info("Non existing user calling for user update") 
  //                     Redirect(controllers.routes.HomeController.index())
  //     }.recover {
  //       case e: CardException =>  Ok(views.html.stripe(UserForms.stripeForm.fill(data))).flashing(("error", "Invalid credentials."))
  //       case e: InvalidRequestException =>  Ok(views.html.stripe(UserForms.stripeForm.fill(data))).flashing(("error", "Invalid credentials."))
  //       case e: AuthenticationException =>  Redirect(controllers.routes.HomeController.index()).flashing(("error", "Oops, service is down."))
  //       case e: APIConnectionException =>  Redirect(controllers.routes.HomeController.index()).flashing(("error", "Oops, service is down."))
  //       case e: StripeException =>  Redirect(controllers.routes.HomeController.index()).flashing(("error", "Oops, service is down."))
  //       case e: Exception =>  Redirect(controllers.routes.HomeController.index()).flashing(("error", "Oops, service is down."))
  //     }
  //   )
  // }
}

