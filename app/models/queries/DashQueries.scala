package models.queries

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import jdub.async.{ Query, Row, FlatSingleRowQuery, Statement }
import jdub.async.queries.BaseQueries
import models.user.{ Role, Dash }
import org.joda.time.LocalDateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object DashQueries extends BaseQueries[Dash] {
  override protected val tableName = "dashes"
  override protected val columns = Seq("id", "clientname", "clientemail", "clientphone", "clientzip", "clientcity", "clientstate", "driverphone", "drivername", "drivercompany","pickuplocation", "attendantnamecomment", "chargedamount", "chargecomment", "other", "created")
  override protected val searchColumns = Seq("id::text", "clientname", "drivername", "clientphone", "driverphone", "clientzip", "clientcity", "drivercompany", "pickuplocation", "chargecomment")

  val insert = Insert
  val getById = GetById
  def searchCount(q: String, groupBy: Option[String] = None) = new SearchCount(q, groupBy)
  //https://github.com/KyleU/jdub-async/blob/master/src/main/scala/jdub/async/queries/BaseQueries.scala
  val search = Search
  val removeById = RemoveById
//TIMESTAMP '2004-10-19 10:23:54+02'
//to_timestamp('05 Dec 2000', 'DD Mon YYYY')
//SELECT EXTRACT(DAY FROM TIMESTAMP '2001-02-16 20:38:40');
//SELECT ~ CAST('20' AS int8) AS "negation";
////SELECT * FROM   ticket WHERE  created >= '2012-12-19 0:0'  AND    created <  '2012-12-20 0:0';
  case class SearchDateRange(startdate: String, enddate: String) extends Query[List[Dash]] {
    val start = LocalDateTime.parse(startdate) 
    val end = LocalDateTime.parse(enddate) 
    override val sql = s"SELECT * FROM $tableName WHERE created >= ? AND created <  ?"
    override val values = Seq(start, end)
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }
//SELECT select_list FROM table_expression [ ORDER BY ... ] [ LIMIT { number | ALL } ] [ OFFSET number ]
  case class SearchAll(filter: String, limit: Option[Int]) extends Query[List[Dash]] {
    override val sql = s"SELECT * FROM $tableName WHERE (clientname || ' ' || drivername || ' ' || drivercompany || ' ' || pickuplocation || ' ' || chargecomment || ' ' || attendantnamecomment || ' ' || clientemail || ' ' || driverphone || ' ' || clientphone)  ILIKE ? LIMIT  ?"
    override val values = Seq(filter, limit.get)
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }

  case class UpdateDash(d: Dash) extends Statement {
    override val sql = updateSql(Seq("clientname", "clientemail", "clientphone", "clientzip", "clientcity", "clientstate", "driverphone", "drivername", "drivercompany","pickuplocation", "attendantnamecomment", "chargedamount", "chargecomment", "other", "created"))
    override val values = {
      Seq(d.clientname, d.clientemail, d.clientphone, d.clientzip, d.clientcity, d.clientstate, d.driverphone, d.drivername, d.drivercompany, d.pickuplocation, d.attendantnamecomment, d.chargedamount, d.chargecomment, d.other, d.created, d.id)
    }
  }

  case class SetClientname(dashId: UUID, clientname: Option[String]) extends Statement {
    override val sql = updateSql(Seq("clientname"))
    override val values = Seq(clientname, dashId)
  }
  case class SetDrverCompany(dashId: UUID, drivercompany: Option[String]) extends Statement {
    override val sql = updateSql(Seq("drivercompany"))
    override val values = Seq(drivercompany, dashId)
  }
  case class FindByID(dashId: UUID) extends FlatSingleRowQuery[Dash] {
    override val sql = getSql(Some("id = ?"))
    override val values = Seq(dashId)
    override def flatMap(row: Row) = Some(fromRow(row))
  }
  case class FindClientByName(clientname: String) extends FlatSingleRowQuery[Dash] {
    override val sql = getSql(Some("clientname = ?"))
    override val values = Seq(clientname)
    override def flatMap(row: Row) = Some(fromRow(row))
  }
  case class FindDriverByName(drivername: String) extends FlatSingleRowQuery[Dash] {
    override val sql = getSql(Some("drivername = ?"))
    override val values = Seq(drivername)
    override def flatMap(row: Row) = Some(fromRow(row))
  }
  case class FindDriverByPhone(driverphone: String) extends FlatSingleRowQuery[Dash] {
    override val sql = getSql(Some("driverphone = ?"))
    override val values = Seq(driverphone)
    override def flatMap(row: Row) = Some(fromRow(row))
  }


  override protected def fromRow(row: Row) = {
    val id = UUID.fromString(row.as[String]("id"))
    val clientname = row.asOpt[String]("clientname")
    val clientemail = row.asOpt[String]("clientemail")
    val clientphone = row.asOpt[String]("clientphone")
    val clientzip = row.asOpt[String]("clientzip")
    val clientcity = row.asOpt[String]("clientcity")
    val clientstate = row.asOpt[String]("clientstate")
    val driverphone = row.asOpt[String]("driverphone")
    val drivername = row.asOpt[String]("drivername")
    val drivercompany = row.asOpt[String]("drivercompany")
    val pickuplocation = row.asOpt[String]("pickuplocation")
    val attendantnamecomment = row.asOpt[String]("attendantnamecomment")
    val chargedamount = row.asOpt[String]("chargedamount")
    val chargecomment = row.asOpt[String]("chargecomment")
    val other = row.asOpt[String]("other")
    val created = row.as[LocalDateTime]("created")
    Dash(id, clientname, clientemail, clientphone, clientzip, clientcity, clientstate, driverphone, drivername, drivercompany, pickuplocation, attendantnamecomment, chargedamount, chargecomment, other, created)
  }

  override protected def toDataSeq(d: Dash) = {
    Seq(d.id, d.clientname, d.clientemail, d.clientphone, d.clientzip, d.clientcity, d.clientstate, d.driverphone, d.drivername, d.drivercompany, d.pickuplocation, d.attendantnamecomment, d.chargedamount, d.chargecomment, d.other, d.created)
  }
}
