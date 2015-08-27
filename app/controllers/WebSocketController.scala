package controllers

import javax.inject.Inject
import com.google.inject.name.Named
// import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
// import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
// import com.mohiva.play.silhouette.api.services.AuthInfoService
// import models.services.UserService
// import models.User
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import akka.util.Timeout
import scala.concurrent.duration._

import actors.WebSocketActor
import actors.TwilioActor._
import actors.CommunicateActor._
import actors.AccountActor._
import actors.StripeActor._

// class WebSocketController @Inject() (
//   val env: Environment[User, SessionAuthenticator],
//   val userService: UserService,
//   val authInfoService: AuthInfoService)
//   (@Named("twilio-actor") twilioActor: ActorRef)
//   (@Named("communicate-actor") commActor: ActorRef)
//   (implicit ec: ExecutionContext)
//   extends Silhouette[User, SessionAuthenticator] {

// implicit val timeout: Timeout = 5.seconds

// 	def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
// 	implicit val req = Request(request, AnyContentAsEmpty)
// 	SecuredRequestHandler { securedRequest =>
// 	  Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
// 		}.map {
// 		  case HandlerResult(r, Some(user)) => Right(WebSocketActor.props(user.lastName.toString, twilioActor, commActor) _)
// 		  case HandlerResult(r, None) => Left(r)
// 		}
// 	}
// }




class WebSocketController @Inject() (
  @Named("twilio-actor") twilioActor: ActorRef,
  @Named("stripe-actor") stripeActor: ActorRef,
  @Named("account-actor") accActor: ActorRef,
  @Named("communicate-actor") commActor: ActorRef ) extends Controller {

	def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
  		WebSocketActor.props(UUID.randomUUID(), twilioActor, commActor, accActor, stripeActor, out)
	}

}