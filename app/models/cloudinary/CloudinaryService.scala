// package models.cloudinary

// import scala.util.Try
// import org.joda.time.DateTime
// import com.cloudinary.Implicits._
// import com.cloudinary.Cloudinary
// import com.cloudinary.response.UploadResponse
// import play.api.libs.concurrent.Execution.Implicits._
// import play.api.Logger
// import scala.concurrent.Future




// trait CloudinaryService {
//   def uploadImage(file: String): Future[UploadResponse]

//   def uploadPhoto(file: String): Future[String]
// }