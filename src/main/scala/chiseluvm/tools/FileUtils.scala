package chiseluvm.tools

import java.io.{File, FileOutputStream, IOException}
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.{DirectoryNotEmptyException, FileAlreadyExistsException, Files, NoSuchFileException, Paths}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

object FileUtils {

  def getListOfFiles(dir: String, ext: String): List[String] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(x => x.isFile && x.getName.endsWith(ext)).map(dir + "/" + _.getName).toList
    } else {
      List("")
    }
  }

  def getFileName(path: String): String = {
    path.substring(path.lastIndexOf("/") + 1)
  }

  def makeFolder(dir: String): Unit = {
    val file = new File(dir + "/")
    file.mkdir()
  }

  def copyFiles(simDir: String, destinationDirPath: String, fileExstension: String, reanemBasedOnDir: Boolean): Unit = {
    new File(destinationDirPath).mkdirs()
    val filesToCopy = getListOfFiles(simDir, fileExstension)
    filesToCopy.foreach { f =>
      try {
        val file = getFileName(f)
        val fileDir = simDir.replace("/logs", "")
        var newName = file
        if (reanemBasedOnDir) {
          newName = fileDir.substring(fileDir.lastIndexOf("/") + 1) + fileExstension
        }
        Files.createFile(Paths.get(destinationDirPath + "/" + newName))
        Files.copy(
          Paths.get(simDir + "/" + file),
          Paths.get(destinationDirPath + "/" + newName),
          REPLACE_EXISTING
        )
      } catch {
        case _: FileAlreadyExistsException =>
          System.out.format("")
        case x: IOException =>
          System.err.format("createFile error: %s%n", x)
      }
    }
  }

  def deleteDirectory(path: String): Unit = {
    if (new File(path).exists) {
      Files.walk(Paths.get(path)).toArray.reverse.filter(_.toString != ".").foreach { f =>
        try {
          Files.delete(Paths.get(f.toString))
        } catch {
          case ex @ (_: IOException | _: NoSuchFileException) => {
            println(s"Something wrong happened while deleting $f in $path")
            throw ex
          }
          case ex: DirectoryNotEmptyException => {
            println(s"Cant' delete non empty directory $f in $path")
            throw ex
          }
        }
      }
    }
  }

  def copyResource(url: URL, toDir: File, targetFileName: String): Unit = {
    val inputStream = url.openStream
    try {
      val outputStream = new FileOutputStream(new File(toDir, targetFileName))
      try {
        outputStream.getChannel().transferFrom(Channels.newChannel(inputStream), 0, Long.MaxValue)
      } finally {
        outputStream.flush()
        outputStream.close()
      }
    } finally {
      inputStream.close()
    }
  }
}
