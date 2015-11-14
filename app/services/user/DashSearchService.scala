package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import jdub.async.Database
import models.queries.DashQueries
import models.user.Dash
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

//Retrieve User by various param types(overloaded)
object DashSearchService {

//clientname, drivername, clientphone, driverphone, clientzip, clientcity, drivercompany, pickuplocation, chargecomment, created
  def search(filter: String, orderby: String = "created", limit: Option[Int] = Some(1000), offset: Option[Int] = Some(0)): Future[List[Dash]] = Database.query(DashQueries.search(filter, orderby, limit, offset))
  //def search(filter: String, orderby: String = "created", limit: Option[Int] = Some(1000), offset: Option[Int] = Some(0)): Future[List[Dash]] = Database.query(DashQueries.SearchAll(filter, limit))
  def searchbydate(startdate: String, enddate: String): Future[List[Dash]] = Database.query(DashQueries.SearchDateRange(startdate, enddate))

  def retrieve(id: UUID): Future[Option[Dash]] = Database.query(DashQueries.getById(Seq(id)))

  def retrievebydrivername(username: String): Future[Option[Dash]] = Database.query(DashQueries.FindDriverByName(username))

  def retrievebyclientname(username: String): Future[Option[Dash]] = Database.query(DashQueries.FindClientByName(username))

  def retrievebydriverphone(phone: String): Future[Option[Dash]] = Database.query(DashQueries.FindDriverByPhone(phone))

}
