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
	case class Message(mid: String, from: String, to: String, date: String, msg: String)
	
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

	// val end = new LocalTime(19, 0, 0, 0)
	// val start = new LocalTime(9, 0, 0, 0)
	// val interval = new Interval(start, end)
	// val open = interval.contains(LocalTime.now())
	// val nowHere = DateTime.now();
	// val nowZur = nowHere.withZone(DateTimeZone.forID("Europe/Zurich"));
	// val interv = new Interval(nowHere, nowZur);
	//DateTimeFormatter simpleTimeFormatter = DateTimeFormat.forPattern("HHmm");
    //LocalTime t1 = LocalTime.parse("0000", simpleTimeFormatter);
 //    LocalTime t2 = LocalTime.MIDNIGHT;
 //    DateTime ny = new DateTime(2011, 2, 2, 7, 0, 0, 0, DateTimeZone.forID("America/New_York"));
	// DateTime la = new DateTime(2011, 2, 3, 10, 15, 0, 0, DateTimeZone.forID("America/Los_Angeles"));
	// Duration duration = new Interval(ny, la).toDuration();
	
	// LocalTime.MIDNIGHT 
	// LocalTime.minusHours(int hours)
	// LocalTime.plusHours(int hours) 
}

class PGActor @Inject() (val env: AuthenticationEnvironment,
							@Named("inbound-actor") inboundActor: ActorRef) extends Actor {

 	import PGActor._

  def receive = LoggingReceive {

		case Message(mid, from, to, date, "start" | " start" | "  start" | "Start" | " Start" | "  Start" | "START" | " START") => 	

      	env.identityService.retrievebyphone(from) onComplete {		
        	case Success(user) =>  if(user.isDefined) {

        		inboundActor ! StartUser(from, date, "start", user.get.fullName.getOrElse("Empty"), user.get.email.getOrElse("Empty"), user.get.phone.getOrElse("Empty"), user.get.address.getOrElse("Empty"),  user.get.hasstripe.getOrElse("Empty"), user.get.preferences.getOrElse("Empty"))
        		} else {
        	 	inboundActor ! StartNone(from, date, "start", "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
        		}
			case Failure(ex) => inboundActor ! StartError(from, date, "start", "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")
		}
		
		// case Message(mid, from, to, date, msg) if (interval.contains(LocalTime.now())) => 
  // 						inboundActor ! OutOfOffice(from, date, msg, "no-fullname", "no-email", "no-phone", "no-address",  "Empty", "no-preferences")



		case Message(mid, from, to, date, msg) => 

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

