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
import akka.event.LoggingReceive
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future




object CommunicateActor {

 trait SerializeMessage {}
	case class Join(user: UUID, out: ActorRef) extends SerializeMessage
	case class UnJoin(user: UUID, out: ActorRef) extends SerializeMessage

	case class BroadCast(trgt: JsValue)

}

// create a supervisor for this statefull actor - move state a level up 
class CommunicateActor extends Actor {

	import CommunicateActor._
	var store:Map[UUID, ActorRef] = Map()

  def receive = LoggingReceive {

	case Join(user, out) =>	save(user, out)

	case UnJoin(user, out) => remove(user)

	case BroadCast(trgt) => broadcast(trgt)
						
  }


  def save(user: UUID, out: ActorRef): UUID = {
    store += (user -> out)
    user
  }

  def remove(user: UUID) = {
    store -= (user)
  }

  def find(user: UUID): Option[ActorRef] = {
    store.get(user)
  }

  def broadcast(trgt: JsValue) = {
    for ((user, actor) <- store) actor ! trgt
  } 

}





