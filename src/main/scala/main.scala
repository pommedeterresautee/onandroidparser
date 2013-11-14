import play.api.libs.json.Json
import scala.io.Source
import scala.reflect.io.Path
import scala.Some

/**
 * Small application to download and parse partition layout files.
 */
object main extends App {
  val URLLayoutList = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/codenames"

  val URLBaseLayoutRaw = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/raw/partlayout4nandroid."
  
  val pathKey = 'path

  case class Device(var codeNames: String, var commercialName: String, var partitionName: Option[Seq[String]] = None) {
    override def toString: String = {
      codeNames + "\n" + partitionName.getOrElse()
    }

    def getAddress = URLBaseLayoutRaw + codeNames
  }

  object Device {
    implicit val format = Json.writes[Device]
  }

  def getSeqPartition(UrlPartLayout: String): Option[String] = {
    UrlPartLayout match {
      case u if u.startsWith("dev:") => None
      case r"(.+)$codeName: (.+)$tail" => Some(codeName)
      case u => throw new Exception("Not expected line in partition layout:\n" + u)
    }
  }

  def getDevice(s: String) = s match {
    case r"(.+)$codeName\t(.+)$commercialName" => Device(codeName, commercialName)
    case _ => throw new Exception("failed on: " + s)
  }

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  val pathToSaveJson = args match {
    case p if p.size == 0 => System.getProperty("user.dir")
    case p if p.size == 1 => p(0)
    case _ => println("Too many arguments.\nJust enter the path where you want to save the file."); sys.exit(1)
  }

  val result = Seq() ++ Source
    .fromURL(URLLayoutList)
    .getLines()
    .map(getDevice)
    .map(x => {
    x.partitionName =
      Option(Seq() ++ Source
        .fromURL(x.getAddress)
        .getLines()
        .flatMap(x => getSeqPartition(x)))
    x
  })
    .zipWithIndex
    .map(i => {
    println("Device " + i._2)
    i._1
  })

  val fileToSave = new java.io.File(pathToSaveJson, "onandroid.json")

  val finalData = Json.prettyPrint(Json.toJson(result))

  Path(fileToSave).toFile.writeAll(finalData)

  println(result.size + " devices parsed and saved to " + fileToSave.getAbsolutePath)
}