package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.google.inject.Inject
import play.api._
import play.api.http.{ContentTypes, HttpEntity}
import play.api.libs.Comet
import play.api.mvc._

class Application @Inject()(materializer: Materializer) extends Controller {

  def index = Action {
    Ok(views.html.cometTest())
  }

  def sendFile() = Action {
    Ok.sendFile(
      content = new java.io.File("public/files/file.pdf"),
      fileName = _ => "helloworld.pdf")
  }

  def sendLargeFile() = Action {
    val file = new java.io.File("public/files/file.pdf")
    val path: java.nio.file.Path = file.toPath
    val source: Source[ByteString, _] = FileIO.fromPath(path)

    val contentLength = Some(file.length())

    Result(
      header = ResponseHeader(200, Map.empty),
      body = HttpEntity.Streamed(source, contentLength, Some("application/pdf"))
    )
  }

  def chuckedString() = Action {
    val source = Source.apply(List("Kiki","foo","bar"))
    Ok.chunked(source)
  }

  def cometString = Action {
    implicit val m = materializer
    def stringSource: Source[String, _] = Source(List("kiki", "foo", "bar"))
    Ok.chunked(stringSource via Comet.string("parent.cometMessage")).as(ContentTypes.HTML)
  }

}