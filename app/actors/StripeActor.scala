package actors

import akka.actor._
import javax.inject._
import com.google.inject.assistedinject.Assisted
import play.api.libs.concurrent.InjectedActorSupport
import com.google.inject.name.Named
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.event.LoggingReceive
import play.api.Logger
import java.util.UUID
import org.joda.time.LocalDateTime
import scala.util.{Success, Failure}
import play.api.Configuration
import actors.AccountActor._
import models.stripe.{StripeService, StripeImpl}
import com.stripe.exception.APIConnectionException
import com.stripe.exception.APIException
import com.stripe.exception.AuthenticationException
import com.stripe.exception.CardException
import com.stripe.exception.InvalidRequestException
import com.stripe.exception.StripeException
import models.stripe.StripeImpl.{ CustomerChargeResponse, CustomerRefundResponse }



object StripeActor {
	case class 	CreateCustomer(userId: UUID, token: String)
	//case class 	ChargeCustomer(phone: String, amt: Int) 
	case class ChargeCustomer(phone: String, typ: String, dateAsPickLoc: String, amt: String, attendantname: String, driverphone: String, date: String)
	case class UpdateDash(phone: String, striperesponse: String, dateAsPickLoc: String, amt: String, attendantname: String, driverphone: String, date: String)
	//case class 	RefundCustomer(chargeId: String)
	case class RefundCustomer(phone: String, typ: String, dateAsPickLoc: String, amt: String, attendantname: String, driverphone: String, date: String)
	case class 	RetrieveCustomer(stripeId: String)
	case class 	DeleteCustomer(userId: UUID)
	case class 	UpdateCustomer(userId: UUID, token: String)

	trait Factory {
    	def apply(key: String): Actor
  	}
}



class StripeActor @Inject() (@Assisted key: String, 
								@Named("account-actor") accActor: ActorRef, 
								@Named("dash-actor") dashActor: ActorRef,
								stripeSer: StripeService) extends Actor {
  import StripeActor._
	import context.dispatcher

  def receive = LoggingReceive {

	case CreateCustomer(userId, token) =>  stripeSer.createCustomer(userId, token)

	case ChargeCustomer(phone, typ, dateAsPickLoc, msg, attendantname, driverphone, date) =>
	//case ChargeCustomer(phone, amt) => 
		val amt = java.lang.Integer.parseInt(msg)
		stripeSer.chargeCustomer(phone, amt) onComplete { //map { cCRes =>
			///cCRes match {
				case Success(cCResponse) =>
					val msg = cCResponse.status + " : " + cCResponse.amount
					accActor ! Notifier(cCResponse.phone, "NOTIFICATION", cCResponse.created, msg, cCResponse.name, "driverphone", true)
					dashActor ! UpdateDash(phone, msg, dateAsPickLoc, amt.toString, attendantname, driverphone, date)
					Logger.info("UpdateDash Line Treated")
				case Failure(ex) => 
					accActor ! Notifier(phone, "NOTIFICATION", date, ex.getMessage(), "charge customer", "driverphone", true)
					//only for testing since all stripe tests are failure responses.
					//dashActor ! UpdateDash(phone, ex.getMessage(), dateAsPickLoc, amt.toString, attendantname, driverphone, date)
					Logger.info("Stripe Failure track: UpdateDash Line Treated")
			//}
		}

	case RefundCustomer(phone, typ, pickLoc, chargeId, attendantname, driverphone, date) =>
 	//case RefundCustomer(chargeId) => 
		stripeSer.refundCustomer(chargeId) onComplete { // map { cCRef =>
			//cCRef match {
				case Success(cRResponse) =>
					val msg = "Refunded" + " : " + cRResponse.amount + "-Bal" + ":" + cRResponse.balance_transaction
					val amt = "-" + cRResponse.amount.toString
					accActor ! Notifier("phone", "NOTIFICATION", cRResponse.created, msg, "refund customer", "driverphone", true)
					dashActor ! UpdateDash(phone, msg, pickLoc, amt, "Admin Refunded:" + chargeId, driverphone, date)
				case Failure(ex) => 
					accActor ! Notifier("phone", "NOTIFICATION", date, ex.getMessage(), "refund customer", "driverphone", true)
			//}
		}

   	case RetrieveCustomer(stripeId) => stripeSer.retrieveCustomer(stripeId)

    case DeleteCustomer(userId) => stripeSer.deleteCustomer(userId)

    case UpdateCustomer(userId, token) => stripeSer.updateCustomer(userId, token)
				
  }
}


class StripeSupervisorActor @Inject() ( childFactory: StripeActor.Factory, @Named("account-actor") accActor: ActorRef ) extends Actor with InjectedActorSupport {
  import StripeActor._
  import context.dispatcher

   		val time = new LocalDateTime() 
		val date = time.toString() 
    //The actor supervisor strategy attempts to send email up to 10 times if there is a EmailException
    override val supervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 300, withinTimeRange = 15 minutes) {
		case e: CardException => accActor ! Notifier("phone", "NOTIFICATION", date, e.getMessage(), "stripe api error - card exception", "driverphone", true); Resume					
	    case e: InvalidRequestException =>  accActor ! Notifier("phone", "NOTIFICATION", date, e.getMessage(), "stripe api error - invalid request", "driverphone", true); Resume					
	    case e: AuthenticationException => accActor ! Notifier("phone", "NOTIFICATION", date, e.getMessage(), "stripe api error - auth exception", "driverphone", true); Restart					
	    case e: APIConnectionException => accActor ! Notifier("phone", "NOTIFICATION", date, e.getMessage(), "stripe api error - api connection", "driverphone", true); Restart					
	    case e: StripeException => accActor ! Notifier("phone", "NOTIFICATION", date, e.getMessage(), "stripe api  error - stripe exception", "driverphone", true); Restart					
	    case e: Exception => accActor ! Notifier("phone", "NOTIFICATION", date, e.getMessage(), "stripe api error - base exception", "driverphone", true); Restart	
      }

     //Forwards messages to child workers - EmailServiceWorker
     val key = "stripeone"
     val child: ActorRef = injectedChild(childFactory(key), key)

    def receive = LoggingReceive {

	    case a: CreateCustomer => child ! a
		case a: ChargeCustomer => child ! a
		case a: RefundCustomer => child ! a
		case a: RetrieveCustomer => child ! a
		case a: DeleteCustomer => child ! a
		case a: UpdateCustomer => child ! a
		case _ => Logger.info("dead letter")
    }
  }