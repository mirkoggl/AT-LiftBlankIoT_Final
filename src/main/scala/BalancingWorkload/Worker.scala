package BalancingWorkload

import akka.actor.ActorPath
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef

abstract class Worker(masterLocation: ActorPath, desc: String)  extends Actor with ActorLogging {
  import MasterWorkerProtocol._

  var descrizione = desc
  var stato : StatusDesc = _

  // We need to know where the master is
  val master = context.actorSelection(masterLocation)

  // Required to be implemented
  def doWork(attribute: String, value: Any): Unit
  
  def printState() : Unit

  // Notify the Master that we're alive

  def numRand : Double =  (Math.random()%3*100).toInt

  def idle: Receive = {
    // Master says there's work to be done, let's ask for it
    
    case WorkerGetStatus =>
      sender ! WorkDescSup(WorkDesc(self, desc, stato), printState) //specializzare con classe opportuna
      
    case WorkerSetStatus(attributo, value) =>
        attributo match { 
          case  "levelPower" => doWork(attributo, value) 
          case  "potenza" => stato = stato.copy(power = value) 
          case  "temperatura" => stato = stato.copy(temp = value)
          case  "pressione" => stato = stato.copy(press = value)
          case  "x" => stato = stato.copy(x = value)
          case  "y" => stato = stato.copy(y = value)
          case  "z" => stato = stato.copy(z = value)
        }
      master ! WorkerDone(WorkDesc(self, descrizione, stato), attributo + " al valore: " + value, printState)
  }
  
  def stop = {
     stato = stato.copy(power = 0) 
     stato = stato.copy(levelPower = 0) 
     stato = stato.copy(temp = 0) 
     println(self + "/" + descrizione + " mi sono spento")
  }

  def receive = idle
}