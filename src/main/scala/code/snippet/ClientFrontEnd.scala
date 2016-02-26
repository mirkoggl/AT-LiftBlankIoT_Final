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
import BalancingWorkload.MasterObj
import BalancingWorkload._
import _root_.net.liftweb.util._
import scala.xml.NodeSeq
import net.liftweb._
import http._
import js._
import JsCmds._
import JE._
import scala.xml.Text
import scala.collection.mutable.MutableList

class ClientFrontEnd {
    import BalancingWorkload.MasterWorkerProtocol._

  var temp : WorkDescSup = _
  var workers : MutableList[WorkDesc] = MutableList()

  def render = {        
        ".clickable [onClick]" #> SHtml.ajaxInvoke {()=> Prova }} 
  
  def getWorkers = {
   implicit val timeout = Timeout(5,TimeUnit.SECONDS)                  
   val future =  MasterObj.m ? GetWorkerId // enabled by the “ask” import         
   val result = Await.result(future, timeout.duration).asInstanceOf[MutableList[WorkDesc]]             
   
   workers = result
  }
  
  def getResult(desc: String) : WorkDescSup = {
   getWorkers
   var workFind = for(i <- workers if (i.desc == desc)) yield i   
   if(workFind.nonEmpty){
     implicit val timeout = Timeout(5,TimeUnit.SECONDS)                  
     val future =  MasterObj.m ? WorkerGet(workFind(0)) // enabled by the “ask” import         
     val result = Await.result(future, timeout.duration).asInstanceOf[WorkDescSup]             
     temp = result
   }
     temp
  }
  
  def returnWorkers : MutableList[WorkDesc] = {
    getWorkers
    workers
  }
  
  def setAttribute(desc : String, attr : String, value : Double) = {
    getWorkers
    var find = for( i <- workers if i.desc == desc) yield i
    MasterObj.m ! WorkerSet(find(0), attr, value)
  }
  
  def checkPower = {
    MasterObj.m ! Poll
  }
  
  def newWorkers = {
    MasterObj.newWorkers
  }
  
  def Prova : JsCmd = {
    var nodeRender : NodeSeq = <p> Actor ID: <span id="id1">Boh</span>
                       <br />Descrizione: <span id="desc1">Boh</span>
                       <br />Temperatura: <span id="temp1">Boh</span>
                       <br />Pressione: <span id="press1">Boh</span>
                       <br />Potenza: <span id="power1">Boh</span>  </p>
    var v = getResult("Lavatrice")
    JsCmds.Alert("Il dispositivo : " + v.wd.desc + "\n" + "la temperatura è di :" 
         + v.wd.status.temp + "\n" + "una pressione di : " + v.wd.status.press + "\n" +
         "una potenza di : " + v.wd.status.power )
  }
}


object ScreenExample extends LiftScreen {
    // here are the fields and default values
    val temp = field("Temp", 0)
    val power = field("Power", 0)
  
  
    // the age has validation rules
    val accens = field("Level", 0, minVal(13, "Too Young"))
  
    def finish() {
      S.notice("Temp: "+temp)
      S.notice("Power: "+power)
      S.notice("Level: "+accens)
    }
  }
