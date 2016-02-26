package code.snippet

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

class CheckButton {

    def onClickCallback(s: String): JsCmd = {
        var cfe = new ClientFrontEnd
        cfe.checkPower
        
        Alert("Controllo avviato!")
   }

    // Note there is no argument list
    def render = {
        "button [onclick]" #> SHtml.onEvent(onClickCallback) 
    }
}