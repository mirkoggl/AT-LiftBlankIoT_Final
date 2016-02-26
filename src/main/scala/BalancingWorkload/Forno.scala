package BalancingWorkload

import akka.actor.ActorPath
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.pipe
import akka.pattern.ask

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class Forno(masterLocation: ActorPath, desc : String) extends Worker(masterLocation, desc) {
    import MasterWorkerProtocol._
  // We'll use the current dispatcher for the execution context.
  // You can use whatever you want.
  implicit val ec = context.dispatcher
  
  descrizione = "Forno" 

  override def preStart() : Unit = {
    stato = StatusDesc(ON, 5, 10, 0, numRand, numRand, numRand)
    master ! WorkerRegister(WorkDesc(self,desc,stato))
    
    println("Worker " + self + " " + descrizione)
    printState
    }
    
  def printState() : Unit = {
    println("Stato forno  " + self.actorRef)
    println("    Potenza: " + stato.power) 
    println("    Temperatura: " + stato.temp) 
    println("    Accensione: " + stato.levelPower) 
  }
  
  def doWork(attribute: String, value: Any): Unit = {
    if(value == OFF)
      stop      
    else if(value == ON)
      stato = stato.copy(levelPower = ON)
    else if (value == LOWPOWER)
      println("Sono un " + descrizione + " e non supporto la modalità risparmio energetico")
  }
  
  def masterOp : PartialFunction[Any, Unit]   = {
    case FornoStart =>
      stato = stato.copy(levelPower = ON)
      stato = stato.copy(temp = 180)
      println(descrizione + " avviato a: " + stato.temp + " gradi")
  }
  
  override def receive = super.receive orElse masterOp

}