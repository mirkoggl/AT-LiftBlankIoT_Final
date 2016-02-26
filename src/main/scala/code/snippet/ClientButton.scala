package code.snippet

import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import net.liftweb.util.Helpers._
import _root_.net.liftweb.util._
import scala.xml.NodeSeq
import net.liftweb._
import http._
import js._
import JsCmds._
import JE._
import scala.xml.Text
import scala.collection.mutable.MutableList

class ClientButton {
  
  def foo: NodeSeq = {
    var x = S.attr("myparam") openOr "myparam: Y U NO DEFINED!?"
    <p>I got {x}!</p>
  }
  
   def renderButton ={ 
     var x = S.attr("x") openOr "myparam: Y U NO DEFINED!?"
     var s : String = """
      |$('#prova"""+ x.toString() +"""').toggle('show');
      """
     "button [onclick]" #> SHtml.ajaxInvoke { () => JsRaw(s.stripMargin) }
   }
}