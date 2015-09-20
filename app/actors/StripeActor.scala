package actors

import akka.actor._
import javax.inject._
import com.google.inject.name.Named
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import akka.event.LoggingReceive
import java.util.UUID
import org.joda.time.LocalDateTime
import scala.util.{Success, Failure}
import play.api.Configuration
import actors.AccountActor._
import models.stripe.{StripeService, StripeImpl}
import models.stripe.StripeImpl.CustomerChargeResponse



object StripeActor {
	case class 	CreateCustomer(userId: UUID, token: String)
	case class 	ChargeCustomer(phone: String, amt: Int) 
	case class 	RefundCustomer(chargeId: String, amt: Int)
	case class 	RetrieveCustomer(stripeId: String)
	case class 	DeleteCustomer(userId: UUID)
	case class 	UpdateCustomer(userId: UUID, token: String)
}

class StripeActor @Inject() ( @Named("account-actor") accActor: ActorRef, stripeSer: StripeService) extends Actor {
  import StripeActor._
	import context.dispatcher

  def receive = LoggingReceive {

	case CreateCustomer(userId, token) =>  stripeSer.createCustomer(userId, token)

	case ChargeCustomer(phone, amt) => 
		val date = new LocalDateTime().toString() 
		stripeSer.chargeCustomer(phone, amt) map { cCRes =>
			cCRes match {
				case Success(cCResponse) =>
					val msg = cCResponse.status + " : " + cCResponse.amount
					accActor ! Notifier(cCResponse.phone, "Notification", cCResponse.created, msg, cCResponse.name, "driverphone", true)
				case Failure(ex) => 
					accActor ! Notifier(phone, "Notification", date, ex.getMessage(), "charge customer", "driverphone", true)
			}
		}
 	case RefundCustomer(chargeId, amt) => stripeSer.refundCustomer(chargeId, amt)

   	case RetrieveCustomer(stripeId) => stripeSer.retrieveCustomer(stripeId)

    case DeleteCustomer(userId) => stripeSer.deleteCustomer(userId)

    case UpdateCustomer(userId, token) => stripeSer.updateCustomer(userId, token)
				
  }
}
