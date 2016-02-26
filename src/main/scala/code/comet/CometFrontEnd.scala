package code.comet

import net.liftweb._
import http._
import SHtml._
import net.liftweb.common.{Box, Full}
import net.liftweb.util._
import net.liftweb.actor._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds.{SetHtml}
import net.liftweb.http.js.JE.Str
import scala.xml.Text
import code.snippet
import code.snippet.ClientFrontEnd
import BalancingWorkload._
import scala.xml.NodeSeq
import scala.collection.mutable.MutableList

class CometFrontEnd extends CometActor {
  
  override def defaultPrefix = Full("comet")
  
  val ident : String = "id"
  val desc : String = "desc"
  val temp : String = "temp"
  val press : String = "press"
  val pot : String = "power"
  val lvlpow : String ="levelpower"
  
  var workList : MutableList[WorkDesc] = (new ClientFrontEnd).returnWorkers
  
  def render = bind("mex" -> renderMex)

  def renderMex = <div>{loopRender}</div>
  
  def loopRender : NodeSeq = {
    var c = new ClientFrontEnd
    workList = c.returnWorkers
    var nodeRender : NodeSeq = <p></p>
    var i : Int = 0
    while( i < workList.length) { 
       var s : String = "lift:ClientButton.renderButton?x=" + i.toString()
       var p : String = "prova" + i.toString()
       var icon : String = setIcon(workList(i))
       nodeRender +:= (<p>
											<a href="#" style="position:relative">
											<img src={icon} style="height:7%; width:7%"/> 
											<button class={s} style="height:15px;width:15px;position:absolute;bottom:40px;right:5px;margin:10;padding:5px 3px;">+</button></a> 
											<a href="#">
                      <span id={p} style="display:none;" align="left"> 
                      <br />Actor ID: <span id={ident + i}>Boh</span>
                      <br />Descrizione: <span id={desc + i}>Boh</span>
                      <br />Temperatura: <span id={temp + i}>Boh</span>
                      <br />Pressione: <span id={press + i}>Boh</span>
                      <br />Potenza: <span id={pot + i}>Boh</span>
                      <br />Level Power: <span id={lvlpow + i}>Boh</span>
                      </span></a></p>
                        )
       i += 1
    }     
    nodeRender
  }
      
  ActorPing.schedule(this, MessageFrontEnd, 1000L)
    
  override def lowPriority : PartialFunction[Any,Unit] = {
    case MessageFrontEnd => {
      var c = new ClientFrontEnd
      workList = c.returnWorkers
      var j : Int = 0
      while( j < workList.length) {
        var temp : String = "temp"+j.toString()
        var power : String = "power"+j.toString()
        var press : String = "press"+j.toString()
        var desc : String = "desc"+j.toString()
        var id : String = "id"+j.toString()
        var lvlp : String = lvlpow+j.toString()
        var pow : String = "Off"
               
        var workSet : WorkDesc = workList(j)
        
        if(workSet.status.levelPower == 1)
          pow = "LowPower"
        else if (workSet.status.levelPower == 2)
          pow = "On"
        
        partialUpdate(SetHtml(temp, Text("" + workSet.status.temp)) &
                      SetHtml(power, Text("" + workSet.status.power)) &
                      SetHtml(id, Text("" + workSet.id)) &
                      SetHtml(desc, Text("" + workSet.desc)) & 
                      SetHtml(press, Text("" + workSet.status.press)) &
                      SetHtml(lvlp, Text("" + pow))             
        )
        j += 1
      }
      ActorPing.schedule(this, MessageFrontEnd, 1000L)
    }
  }
  
  def setIcon (worker : WorkDesc) : String = {
    var res : String = "/images/lavatrice-icon.png"
    
    if(worker.desc.contains("Forno"))
      res = "/images/microonde-icon.png"
    else if (worker.desc.contains("Termostato"))
        res = "/images/aria-icon.png"
    else if (worker.desc.contains("Televisore"))
      res = "/images/tc-icon.png"
    
    res
  }
}
case object MessageFrontEnd