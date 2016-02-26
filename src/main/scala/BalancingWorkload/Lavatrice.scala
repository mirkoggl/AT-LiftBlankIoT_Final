package BalancingWorkload

import akka.actor.ActorPath
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.pipe
import akka.pattern.ask

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class Lavatrice (masterLocation: ActorPath, desc : String) extends  Worker(masterLocation, desc){
    import MasterWorkerProtocol._
    
  var lavaggio = "Cotone"
   
  def doWork(attribute: String, value: Any): Unit = {
    if(value == OFF)
      stop     
    else if (value == LOWPOWER){
      stato = stato.copy(levelPower = LOWPOWER)
      println(self + "/" + descrizione + " in risparmio energetico")
    }
    else if (value == ON)
      stato = stato.copy(levelPower = ON)
  }
    
   override def preStart() : Unit = {
    stato = StatusDesc(ON, 2, 50, 0, numRand, numRand, numRand)
    master ! WorkerRegister(WorkDesc(self,desc,stato))
    println("Worker " + self + " " + descrizione)
    printState
   }
    
  def masterOp : PartialFunction[Any, Unit]   = {
    case LavatriceSetLavaggio(value) => 
       lavaggio = value
  }
  
  def printState() : Unit = {
    println("Stato Lavatrice  " + self) 
    println("    Accensione: " + stato.levelPower) 
    println("    Potenza: " + stato.power)
    println("    Lavaggio: " + lavaggio) 
  }
 
  override def receive = super.receive orElse masterOp  

}