package actors

import akka.actor._
import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import javax.inject._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.util.UUID
import org.joda.time.LocalDateTime
import scala.util.Random
import play.api.Logger
import actors.PGActor._
import actors.StripeActor._
import actors.TwilioActor._
import actors.CommunicateActor._
import javax.inject._
import com.google.inject.name.Named
import akka.event.LoggingReceive
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

object AccountActor {

	case class MarkTaken(from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)
	case class MarkCompleted(from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)

	case class Accounter(from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)
	case class Notifier(from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)
	case class NoBuffer(from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)

	case object Refresh

	case object Recall

	case class Outs(id: String, from: String, typ: String, date: String, msg: String, name: String, driverphone: String, isDone: Boolean)
	object Outs{
	  implicit val insReads: Reads[Outs] = (
	  	(JsPath \ "id").read[String] and
	    (JsPath \ "from").read[String] and
	    (JsPath \ "typ").read[String] and
	    (JsPath \ "date").read[String] and
	    (JsPath \ "msg").read[String] and
	    (JsPath \ "name").read[String] and
	    (JsPath \ "driverphone").read[String] and
	    (JsPath \ "isDone").read[Boolean]
	  )(Outs.apply _)

	  implicit val insWrites: Writes[Outs] = (
	  	(JsPath \ "id").write[String] and
	    (JsPath \ "from").write[String] and
	    (JsPath \ "typ").write[String] and
	    (JsPath \ "date").write[String] and
	    (JsPath \ "msg").write[String] and
	    (JsPath \ "name").write[String] and
	    (JsPath \ "driverphone").write[String] and
	    (JsPath \ "isDone").write[Boolean]
	  )(unlift(Outs.unapply))
	}

}
// create a supervisor for this statefull actor - move state a level up 
class AccountActor @Inject() ( @Named("communicate-actor") commActor: ActorRef)  extends Actor {

	import AccountActor._
	val msgHold = new ListBuffer[Outs]()
	val notHold = new ListBuffer[Outs]()
	var accHold = new ListBuffer[Outs]()

  def receive = LoggingReceive {

	case Recall => 	recall

	case Refresh => refresh

	case MarkTaken(from, typ, date, msg, name, driverphone, isDone) => Logger.info("MarkTaken called")
		val time = new LocalDateTime()
		val created = time.toString()

		accHold.find(_.from == from) match {
			case Some(obj) => Logger.info(accHold.indexWhere(_.from == obj.from).toString)
			accHold.update(accHold.indexOf(obj), (Outs(obj.id, obj.from, obj.typ, created, obj.msg, obj.name, obj.driverphone, true)))
			recall
			case None =>
		}
		recall

	case MarkCompleted(from, typ, date, msg, name, driverphone, isDone) => Logger.info("MarkCompleted Called")
		val group = msgHold.filter(_.from == from)
		group.foreach{obj => 
			Logger.info(msgHold.indexWhere(_.id == obj.id).toString)
			Logger.info(msgHold.indexOf(obj).toString)
		msgHold.update(msgHold.indexOf(obj), (Outs(obj.id, obj.from, obj.typ, obj.date, obj.msg, obj.name, obj.driverphone, true)))
		}
		recall


	case Accounter(from, typ, date, msg, name, driverphone, isDone) => 
											val id = UUID.randomUUID.toString()
											val obj = Outs(id, from, typ, date, msg, name, driverphone, isDone)
											msgPush(obj)	


	case Notifier(from, typ, date, msg, name, driverphone, isDone) => 
											val id = UUID.randomUUID.toString()
											val obj = Outs(id, from, typ, date, msg, name, driverphone, isDone)
											notPush(obj)	

	case NoBuffer(from, typ, date, msg, name, driverphone, isDone) => 
											val id = UUID.randomUUID.toString()
											val obj = Outs(id, from, typ, date, msg, name, driverphone, isDone)
											commActor ! BroadCast(Json.toJson(obj))	



	}

	  def msgPush(obj: Outs) = {
	    msgHold += obj
	    recall
	  }

	  def notPush(obj: Outs) = {
	    notHold += obj
	    recall
	  }

	  def recall = {
	  	Logger.info("Recall called")
	  	accRecal
	    for (obj <- notHold) commActor ! BroadCast(Json.toJson(obj))
	    for (obj <- accHold) commActor ! BroadCast(Json.toJson(obj))
	    for (obj <- msgHold) commActor ! BroadCast(Json.toJson(obj))
	  } 

	  def refresh = {
	  	Logger.info("Refresh called")
	    msgHold.clear
	    accHold.clear
	    notHold.clear
	  } 

	  def accRecal = {
	  	Logger.info("Recalculate called")
		msgHold.foreach(item => 
			if (!accHold.exists(_.from == item.from)) {
				val grp = msgHold.filter(_.from == item.from)
				val msg = grp.filter(_.isDone == false).length

				val time = new LocalDateTime()
				val created = time.toString()
				Logger.info("new ac created")
			accHold += Outs(item.from, item.from, "ACCOUNTS", created, msg.toString, item.name, "driverphone", false)

			}else{
				val grp = msgHold.filter(_.from == item.from)
				val msg = grp.filter(_.isDone == false).length
				val time = new LocalDateTime()
				val created = time.toString()

				accHold.find(_.from == item.from) match {
					case Some(obj) =>	Logger.info(accHold.indexWhere(_.from == obj.from).toString)
					accHold.update(accHold.indexOf(obj), (Outs(obj.id, obj.from, obj.typ, created, msg.toString, obj.name, obj.driverphone, obj.isDone)))
					case None =>	Logger.info("Cliams no ac obj found")
				}
			}
		)
	}

}


//accHold.indexOf(obj)
//accHold.indexWhere(_.from == obj.from)

// groupBy[K](f: (A) ⇒ K): immutable.Map[K, ListBuffer[A]]
// .filter(x => x != "Noun")


// foreach(f: (A) ⇒ Unit): Unit

// find(p: (A) ⇒ Boolean): Option[A]

// filterNot(p: (A) ⇒ Boolean): ListBuffer[A]

// filter(p: (A) ⇒ Boolean): ListBuffer[A]

// exists(p: (A) ⇒ Boolean): Boolean

// contains[A1 >: A](elem: A1): Boolean

// sortBy[B](f: (A) ⇒ B)(implicit ord: math.Ordering[B]): ListBuffer[A]

// update(n: Int, x: A): Unit



// val evens = x.filter(_ % 2 == 0)
// originalList.filter(_ > 2)
// peeps.filter(_.last == "Flintstone").map(_.first)
// fruits.filter(_.length > 5)