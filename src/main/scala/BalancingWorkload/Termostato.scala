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

class Termostato (masterLocation: ActorPath, desc : String) extends  Worker(masterLocation, desc) {
    import MasterWorkerProtocol._
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
 
  def doWork(attribute: String, value: Any): Unit = {
    if(value == OFF)
      println("Sono un termostato ed è meglio se non mi spengo!")     
    else
      println("Sono un termostato e non faccio niente!")
  }
    
   override def preStart() : Unit = {
    stato = StatusDesc(ON, 20, 100, 0, numRand, numRand, numRand)
    master ! WorkerRegister(WorkDesc(self,desc,stato))
    println("Worker " + self + " " + descrizione)
    printState
   }
    
  def masterOp : PartialFunction[Any, Unit]   = {
    case TermostatoSetTemperatura(value) => 
       stato = stato.copy(temp = value)
       
    case TermostatoStart =>
      stato = stato.copy(levelPower = ON)
      stato = stato.copy(temp = 25)
      println(descrizione + "in funzione. \nTemperatura ambiente impostata a: " + stato.temp + " gradi")
  }
  
 def printState() : Unit = {
    println("Stato Termostato  " + self) 
    println("    Temperatura: " + stato.temp) 
    println("    Potenza: " + stato.power) 
    println("    Accensione: " + stato.levelPower) 
  }
  override def receive = super.receive orElse masterOp
}