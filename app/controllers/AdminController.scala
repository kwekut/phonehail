package controllers

import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import models.user.{ User, Dash, DashDateData, UserForms, AdminUserUpdateData, AdminSearchData, AdminDashUpdateData, Role }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import java.util.UUID
import org.joda.time.LocalTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.LocalDateTime
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.JavaConversions._
import scala.concurrent.Future
import play.api.Logger
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@javax.inject.Singleton
class AdminController @javax.inject.Inject() (
  val messagesApi: MessagesApi, 
  val env: AuthenticationEnvironment) extends Silhouette[User, CookieAuthenticator] {
  

  def adminIndex = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          Future.successful(Ok(views.html.adminindex(request.identity)))
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        case None =>
          Future.successful(Redirect(controllers.routes.HomeController.index()))
      }    
  }

// <a href='@routes.AdminController.adminDashDelete(dash.id)'><b>Delete</b></a>

  def dashSearch = SecuredAction { implicit req =>
    UserForms.dashSearchForm.bindFromRequest.fold(
      form => Redirect(routes.AdminController.adminDashList("a")),
      filter => Redirect(routes.AdminController.adminDashList(if(filter.nonEmpty){filter}else{"*"}))
    )
  }


  def adminDashList(filter:String="a")  = SecuredAction.async {
    //http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
    env.dashSearchService.search(filter).flatMap {
     dashes =>  
      val vars = Analyzer(dashes)
      Future.successful {Ok(views.html.admindashlist(
        dashes, vars.volumesum.toString, vars.revenuesum.toString, vars.uniqueCustomers.toString, vars.averepeatRateCustomers.toString, 
        vars.averageSpendCustomer.toString, vars.pickuplocations.toString, vars.uniqueDrivers.toString, vars.uniqueCompanies.toString, 
        vars.maxDay.toString, vars.maxHour.toString, vars.maxMonth.toString, vars.maxLocation.toString, 
        UserForms.dashSearchForm, filter.toString, UserForms.dashDateSearchForm))
      }
    }
  }

//to_timestamp('05 Dec 2000', 'DD Mon YYYY')
//SELECT EXTRACT(DAY FROM TIMESTAMP '2001-02-16 20:38:40');
//SELECT ~ CAST('20' AS int8) AS "negation";
////SELECT * FROM   ticket WHERE  created >= '2012-12-19 0:0'  AND    created <  '2012-12-20 0:0';
  // bind the form request.
//http://stackoverflow.com/questions/14007541/playframework-form-and-two-submit-buttons
  def adminDashDateList  = SecuredAction.async { implicit request =>
    Logger.info(request.body.asText.map { text => text}.getOrElse {"none"})

    UserForms.dashDateSearchForm.bindFromRequest.fold(
      form => Future.successful{Redirect(routes.AdminController.adminDashList("*"))},
      filters => 
    // Prevent errors because of submitted empty forms
    if(filters.startmonth.length != 2 || filters.endmonth.length != 2 || filters.startyear.length != 4 || filters.endyear.length != 4 ){
      Future.successful{Redirect(routes.AdminController.adminDashList("*"))}
    } else {
      //Check which submit button was chosen, and do recommended action
      Future{request.body.asFormUrlEncoded.flatMap(m => m.get("action").flatMap(_.headOption))} flatMap {

        case None => Future.successful{Redirect(routes.AdminController.adminDashList("*"))}
        
        case Some("Export") => {
            val startday = "01"
            val f = filters.endmonth
            val g = filters.endyear.toInt//_.toInt
            val endday = if (f=="04"){"30"} else if (f=="09"){"30"} else if (f=="11"){"30"} else if (f=="06"){"30"} else if ( (f=="02") && (g%4==0) ){"29"} else if (f=="02"){"28"} else {"31"}
            val time = "00:00:00"
            val startdate = filters.startyear + "-" + filters.startmonth + "-" + startday //+ " " + time
            val enddate = filters.endyear + "-" + filters.endmonth + "-" + endday //+ " " + time
                exportExcel(startdate, enddate)
        }

        case Some("Search") => {
        val startday = "01"
        val f = filters.endmonth
        val g = filters.endyear.toInt
        val endday = if (f=="04"){"30"} else if (f=="09"){"30"} else if (f=="11"){"30"} else if (f=="06"){"30"} else if ( (f=="02") && (g%4==0) ){"29"} else if (f=="02"){"28"} else {"31"}
        val time = "00:00:00"
        val startdate = filters.startyear + "-" + filters.startmonth + "-" + startday //+ " " + time
        val enddate = filters.endyear + "-" + filters.endmonth + "-" + endday //+ " " + time
          env.dashSearchService.searchbydate(startdate, enddate).flatMap {
                 dashes => 
                  val vars = Analyzer(dashes)
            val info = DashDateData(filters.startmonth, filters.startyear, filters.endmonth, filters.endyear)
            Future.successful { Ok(views.html.admindashlist(
            dashes, vars.volumesum.toString, vars.revenuesum.toString, vars.uniqueCustomers.toString, vars.averepeatRateCustomers.toString, 
            vars.averageSpendCustomer.toString, vars.pickuplocations.toString, vars.uniqueDrivers.toString, vars.uniqueCompanies.toString, 
            vars.maxDay.toString, vars.maxHour.toString, vars.maxMonth.toString, vars.maxLocation.toString, 
            UserForms.dashSearchForm, "filter", UserForms.dashDateSearchForm.fill(info)))
            }
          }
        }
        case _ => Future.successful{Redirect(routes.AdminController.adminDashList("a"))}
      }
    }     
    )
  }

  def adminSearch = SecuredAction { implicit req =>
    UserForms.adminSearchForm.bindFromRequest.fold(
      form => Redirect(routes.AdminController.adminUserList("1")),
      filter => Redirect(routes.AdminController.adminUserList(filter))
    )
  }

  def adminUserList(filter:String="1")  = SecuredAction.async {
    env.identityService.search(filter).flatMap {
     users => Future.successful {Ok(views.html.adminuserlist(users, UserForms.adminSearchForm))}
    }
  }

  def adminUserShow(userId: UUID) = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
            env.identityService.retrieve(userId).flatMap {
              case Some(user) => Future.successful(Ok(views.html.adminusershow(user))) 
              case None => Future.successful(Redirect(controllers.routes.AdminController.adminUserList("1"))) 
          }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  }


  def adminDashUpdateForm(dashid: UUID) = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          env.dashSearchService.retrieve(dashid).flatMap {
            case Some(user) => 
        val info = AdminDashUpdateData(user.id.toString, user.clientname.getOrElse(""), user.clientemail.getOrElse(""), 
          user.clientphone.getOrElse(""), user.clientzip.getOrElse(""), user.clientcity.getOrElse(""), user.clientstate.getOrElse(""), 
          user.driverphone.getOrElse(""), user.drivername.getOrElse(""), user.drivercompany.getOrElse(""), user.pickuplocation.getOrElse(""), 
          user.attendantnamecomment.getOrElse(""), user.chargedamount.getOrElse(""), user.chargecomment.getOrElse(""),
          user.other.getOrElse(""))
        val filledForm = UserForms.adminDashUpdateForm.fill(info)
        Future.successful(Ok(views.html.admindashupdate(request.identity, filledForm))) 
            case None =>
        Future.successful{Redirect(controllers.routes.AdminController.adminDashList("1"))} 
        }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
      case None =>
        Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }

  def adminDashUpdate = SecuredAction.async { implicit request =>
    UserForms.adminDashUpdateForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.admindashupdate(request.identity, form))),
      data => {
        env.identityService.retrieve(request.identity.id).flatMap {
          case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          env.dashSearchService.retrieve(UUID.fromString(data.id)).flatMap {
            case Some(dash) =>  updateDash(data, dash)

            case None =>
              Future.successful{Redirect(controllers.routes.AdminController.adminDashList("1"))}
          }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
          case None =>  Future.successful {  Ok(views.html.index())  }
        }
      }
    )
  }


  def adminUserUpdateForm(userId: UUID) = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          env.identityService.retrieve(userId).flatMap {
            case Some(user) => 
        val info = AdminUserUpdateData(user.username.getOrElse(""), user.roles.mkString, user.email.getOrElse(""), 
          user.phone.getOrElse(""), user.street.getOrElse(""), user.city.getOrElse(""), user.state.getOrElse(""), 
          user.zip.getOrElse(""), user.fullName.getOrElse(""), user.hasstripe.getOrElse(""), 
          user.preferences.getOrElse(""))
        val filledForm = UserForms.adminUserUpdateForm.fill(info)
        Future.successful(Ok(views.html.adminuserupdate(request.identity, filledForm))) 
            case None =>
        Future.successful{Redirect(controllers.routes.AdminController.adminUserList("1"))} 
        }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
      case None =>
        Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }

  def adminUserUpdate = SecuredAction.async { implicit request =>
    UserForms.adminUserUpdateForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.adminuserupdate(request.identity, form))),
      data => {
        env.identityService.retrieve(request.identity.id).flatMap {
          case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          env.identityService.retrieve(data.email).flatMap {
            case Some(user) =>  updateUser(data, user)

            case None =>
              Future.successful{Redirect(controllers.routes.AdminController.adminUserList("1"))}
          }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
          case None =>  Future.successful {  Ok(views.html.index())  }
        }
      }
    )
  }


  private[this] def updateUser(data: AdminUserUpdateData, user: User)(implicit request: SecuredRequest[AnyContent]) = {
    val position = Role(data.roles) 
    

    val updateduser = user.copy(
      username = Some(data.username),
      roles =  Set(position),
      email =  Some(data.email),
      phone =  Some(data.phone),
      street = Some(data.street),
      city = Some(data.city),
      state = Some(data.state),
      zip = Some(data.zip),
      fullName =  Some(data.fullName),
      hasstripe =  Some(data.hasstripe),
      preferences =  Some(data.preferences)
    )
    for {
      u <- env.userService.save(updateduser, update = true)
    } yield {
      Redirect(controllers.routes.AdminController.adminUserList("1"))
    }
  }


  private[this] def updateDash(data: AdminDashUpdateData, dash: Dash)(implicit request: SecuredRequest[AnyContent]) = {
// Dont update client data because a charge is only supposed to be sucessful if there is complete client data. And
// the dash data is only saved when a transaction(charge) is sucessfully completed
    val updateddash = dash.copy(
      //driverphone = Some(data.driverphone),
      drivername = Some(data.drivername),
      drivercompany =  Some(data.drivercompany),
      pickuplocation =  Some(data.pickuplocation),
      attendantnamecomment =  Some(data.attendantnamecomment),
      //chargedamount =  Some(data.chargedamount),
      //chargecomment =  Some(data.chargecomment),
      other =  Some(data.other)
      //created =  Some(data.created)
    )
    for {
      u <- env.dashService.save(updateddash, update = true)
    } yield {
      Redirect(controllers.routes.AdminController.adminDashList("1"))
    }
  }
// The excel export function for the searchers above
//http://beginnersbook.com/2014/01/how-to-write-to-a-file-in-java-using-fileoutputstream/
//https://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/Cell.html
// target button to controller
  private[this] def exportExcel(startdate: String, enddate: String)(implicit request: SecuredRequest[AnyContent]) = {
                val workbook: XSSFWorkbook = new XSSFWorkbook();
                val sheet: XSSFSheet = workbook.createSheet("Sheet1");

              env.dashSearchService.searchbydate(startdate, enddate).flatMap {
                dashes =>
                for ((dash, index) <- dashes.zipWithIndex) {
                  val row = sheet.createRow(index.toShort);
                  row.createCell(0.toShort).setCellValue( (if(dash.created.toString.nonEmpty){dash.created.toString}else{"none"}): String);
                  row.createCell(1.toShort).setCellValue( (dash.clientname.getOrElse("none")): String);
                  row.createCell(2.toShort).setCellValue( (dash.clientphone.getOrElse("none")): String);
                  row.createCell(3.toShort).setCellValue( (dash.clientemail.getOrElse("none")): String);
                  row.createCell(4.toShort).setCellValue( (dash.clientcity.getOrElse("none")): String);
                  row.createCell(5.toShort).setCellValue( (dash.clientstate.getOrElse("none")): String);
                  row.createCell(6.toShort).setCellValue( (dash.clientzip.getOrElse("none")): String);
                  row.createCell(7.toShort).setCellValue( (dash.drivername.getOrElse("none")): String);
                  row.createCell(8.toShort).setCellValue( (dash.driverphone.getOrElse("none")): String);
                  row.createCell(9.toShort).setCellValue( (dash.drivercompany.getOrElse("none")): String);
                  row.createCell(10.toShort).setCellValue( (dash.pickuplocation.getOrElse("none")): String);
                  row.createCell(11.toShort).setCellValue( (dash.attendantnamecomment.getOrElse("none")): String);
                  row.createCell(12.toShort).setCellValue( (dash.chargedamount.map(x => x.toInt).getOrElse(0)): Int );
                  row.createCell(13.toShort).setCellValue( (dash.chargecomment.getOrElse("none")): String);
                  row.createCell(14.toShort).setCellValue( (dash.other.getOrElse("none")): String);
                }

                //val file = new java.io.File("C:/Users/Public/Documents/fileToServe.xlsx")
                val file = new java.io.File("/tmp/fileToServe.xlsx")
                val fos = new FileOutputStream(file)
                workbook.write(fos)

                Future.successful {Ok.sendFile(
                  content = file,
                  fileName = _ => "dashfile.xlsx"
                )
              }
            }
  }
// All the statistical calculations have been abstracted into this class object
//http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
//http://ktuman.blogspot.com/2009/10/how-to-simply-sum-values-in-map-in.html
//http://markusjais.com/the-groupby-method-from-scalas-collection-library/
//http://stackoverflow.com/questions/24216479/scala-reduce-function
    //http://ideone.com/dyrkYM
  case class Analyzer(dashes: List[Dash])  {
      val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-mm-dd HH:MM:SS")
        val volume = new ListBuffer[Int]()
        val revenue = new ListBuffer[Int]()
        var piclocCum = new ListBuffer[String]()
        var cusCum = new ListBuffer[String]()
        var cushomCum = new ListBuffer[String]()
        var drivCum = new ListBuffer[String]()
        var drivComCum = new ListBuffer[String]()
        var chargCum = new ListBuffer[String]()
        var dayCum = new ListBuffer[String]()
        var mnthCum = new ListBuffer[String]()
        var hrCum = new ListBuffer[String]()  

          dashes foreach { dash=>
              volume += 1
              revenue += (dash.chargedamount.map(x => x.toInt).getOrElse(0))
              piclocCum += (dash.pickuplocation.getOrElse("none"))
              cusCum += (dash.clientname.getOrElse("none"))
              cushomCum += (dash.clientzip.getOrElse("none"))
              drivCum += (dash.drivername.getOrElse("none"))
              drivComCum += (dash.drivercompany.getOrElse("none"))
              chargCum += (dash.chargedamount.getOrElse("none"))
              dayCum += (if(dash.created.toString.isEmpty){"none"}else{(df.parseDateTime(dash.created.toString("yyyy-mm-dd HH:MM:SS"))).toString("E")})
              mnthCum += (if(dash.created.toString.isEmpty){"none"}else{(df.parseDateTime(dash.created.toString("yyyy-mm-dd HH:MM:SS"))).toString("M")})
              hrCum += (if(dash.created.toString.isEmpty){"none"}else{(df.parseDateTime(dash.created.toString("yyyy-mm-dd HH:MM:SS"))).toString("k")})
           }

        def pickupLocationTally = piclocCum groupBy(word => word) mapValues(_.size)
        def customerNameTally = cusCum groupBy(word => word) mapValues(_.size)
        def customerHomeTally = cushomCum groupBy(word => word) mapValues(_.size)
        def driverNameTally= drivCum groupBy(word => word) mapValues(_.size)
        def driverCompanyTally = drivComCum groupBy(word => word) mapValues(_.size)
        def chargeTally = chargCum groupBy(word => word) mapValues(_.size)
        def dayTally = dayCum groupBy(word => word) mapValues(_.size)
        def monthTally = mnthCum groupBy(word => word) mapValues(_.size)
        def hourTally = hrCum groupBy(word => word) mapValues(_.size)
        def averepeatRateCustomers = if ( customerNameTally.size != 0) {customerNameTally.foldLeft(0)(_+_._2) / customerNameTally.size.toDouble} else {0}
        def averageSpendCustomer = if ( customerNameTally.size != 0) { revenue.sum / customerNameTally.size.toDouble} else {0}
        def uniqueCustomers = customerNameTally.size
        def pickuplocations = pickupLocationTally.size
        def uniqueDrivers = driverNameTally.size
        def uniqueCompanies = driverCompanyTally.size
        def revenuesum = revenue.sum
        def volumesum = volume.sum
        def maxDay = dayTally//.max
        def maxHour = hourTally//.max
        def maxMonth = monthTally//.max
        def maxLocation = pickupLocationTally
    }

}

