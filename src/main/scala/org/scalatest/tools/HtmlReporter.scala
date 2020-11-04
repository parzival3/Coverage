package org.scalatest.tools

private[tools] object HtmlReporter {
  final val SUCCEEDED_BIT = 1
  final val FAILED_BIT = 2
  final val IGNORED_BIT = 4
  final val PENDING_BIT = 8
  final val CANCELED_BIT = 16

  def convertSingleParaToDefinition(html: String): String = {
    val firstOpenPara = html.indexOf("<p>")
    if (firstOpenPara == 0 && html.indexOf("<p>", 1) == -1 && html.indexOf("</p>") == html.length - 4)
      html.replace("<p>", "<dl>\n<dt>").replace("</p>", "</dt>\n</dl>")
    else html
  }

  def convertAmpersand(html: String): String =
    html.replaceAll("&", "&amp;")
}
