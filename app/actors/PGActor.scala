package actors

import akka.actor._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import play.api.Logger
import akka.event.LoggingReceive
import services.user.AuthenticationEnvironment
import org.joda.time.LocalTime
import org.joda.time.Interval
import org.joda.time.LocalDateTime
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject._
import com.google.inject.name.Named

object PGActor {
	case class Message(mid: String, from: String, date: String, msg: String)
	
	class StorageException(msg: String) extends RuntimeException(msg)

	trait UserParent { def from: String }
	case class StartUser(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class StartNone(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class StartError(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class GenUser(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class GenNone(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class GenError(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class HelpUser(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class HelpNone(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class HelpError(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent
	case class OutOfOffice(from: String, date: String, msg: String, fullname: String, email: String, phone: String, address: String,  hasstripe: String, preferences: String) extends UserParent


	val nowLA = DateTime.now(DateTimeZone.forID("America/Los_Angeles")) 
	 val midnightHere = DateTime.now().withTimeAtStartOfDay
	 val midnightLA = midnightHere.withZone(DateTimeZone.forID("America/Los_Angeles"))
	val startLA = midnightLA.minusHours(5)
	val endLA = midnightLA.plusHours(7)
	val interval = new Interval(startLA, endLA)
	// val open = interval.contains(nowLA) 

}

class PGActor @Inject() (val env: AuthenticationEnvironment,
							@Named("inbound-actor") inboundActor: ActorRef) extends Actor {

 	import PGActor._

  def receive = LoggingReceive {

		case Message(mid, from, date, "start" | " start" | "  start" | "Start" | " Start" | "  Start" | "START" | " START") => 	

      	env.identityService.retrievebyphone(from) onComplete {		
        	case Success(user) =>  if(user.isDefined) {

        		inboundActor ! StartUser(from, date, "start", user.get.fullName.getOrElse("Empty"), user.get.email.getOrElse("Empty"), user.get.phone.getOrElse("Empty"), user.get.address.getOrElse("Empty"),  user.get.hasstripe.getOrElse("Empty"), user.get.preferences.getOrElse("Empty"))
        		} else {
        	 	inboundActor ! StartNone(from, date, "start", "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
        		}
			case Failure(ex) => inboundActor ! StartError(from, date, "start", "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
		}
		
		// case Message(mid, from, to, date, msg) if (!interval.contains(nowLA)) => 
  // 						inboundActor ! OutOfOffice(from, date, msg, "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")



		case Message(mid, from, date, msg) => 

      	env.identityService.retrievebyphone(from) onComplete {		
        	case Success(user) =>  Logger.info(s"user retrieve by phone ${user}")
        	if(user.isDefined) {

        			inboundActor ! GenUser(from, date, msg, user.get.fullName.getOrElse("Empty"), user.get.email.getOrElse("Empty"), user.get.phone.getOrElse("Empty"), user.get.address.getOrElse("Empty"),  user.get.hasstripe.getOrElse("Empty"), user.get.preferences.getOrElse("Empty"))
        		} else {
        	 		inboundActor ! GenNone(from, date, msg, "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
        		}
			case Failure(ex) => inboundActor ! GenError(from, date, msg, "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
		}

 	}
}

