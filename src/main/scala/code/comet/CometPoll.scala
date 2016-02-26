package code.comet

import scala.language.postfixOps
import _root_.net.liftweb._
import http._
import common._
import util._
import Helpers._
import js._
import JsCmds._
import _root_.scala.xml.{Text, NodeSeq}
import code.snippet.ClientFrontEnd
import BalancingWorkload.WorkDesc
import scala.collection.mutable.MutableList

class CometPoll extends CometActor {
  private var messages: MutableList[WorkDesc] = MutableList()

  var workList : MutableList[WorkDesc] = (new ClientFrontEnd).returnWorkers
  var temp : Double = 0.0
  override def defaultPrefix = Full("cometpoll")
  //style="position:relative;bottom:645px;right:600px;"
   def renderMessages = <div align="center">{loopRender} <h3>Temperatura ambientale: <span id="temp">{temp}</span></h3></div>
    
   def loopRender : NodeSeq = {
    var c = new ClientFrontEnd
    workList = c.returnWorkers
    var nodeRender : NodeSeq = <p></p>
    var i : Int = 0
    var pot : Double = 0.0
    
    while( i < workList.length) { 
      pot += workList(i).status.power
      i += 1
    }     
    nodeRender +:= (<p><h3>Potenza Complessiva: <span id="potenza">{pot}</span></h3></p>)
    nodeRender
  }  
  
  ActorPing.schedule(this, MessagePoll, 1000L)

  def render =  bind("messages" -> renderMessages)
  
  override def lowPriority = {
  case MessagePoll => {
    var i : Int = 0
    var pot : Double = 0.0
    
    while( i < workList.length) { 
      pot += workList(i).status.power
      i += 1
    }     
    var term = for(i <- workList if(i.desc=="Termostato") ) yield i
    temp=term(0).status.temp
    
    partialUpdate(SetHtml("potenza", Text("" + pot)) & 
                  SetHtml("temp", Text("" + temp)))

    ActorPing.schedule(this, MessagePoll, 1000L)
  }
  }
}

case object MessagePoll
