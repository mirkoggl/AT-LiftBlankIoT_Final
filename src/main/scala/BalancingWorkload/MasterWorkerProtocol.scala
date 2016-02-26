package BalancingWorkload

import akka.actor.ActorRef

case class WorkDesc(id: ActorRef, desc: String, status : StatusDesc)
case class WorkDescSup(wd : WorkDesc, printState:() => Unit)
case class StatusDesc(levelPower : Int, temp : Double, power : Double, press : Double, x : Double, y: Double, z : Double)
case class Poll()
case class GetWorkerId()

object MasterWorkerProtocol {
  
  def ON = 2
  def LOWPOWER = 1
  def OFF = 0
  
  // Messages from Admin
  case object Start
  
  // Messages from Client to Master
  case class WorkerStart(worker: ActorRef)
  case class ClientWorkerStop(worker: ActorRef)
  case class WorkerSet(workerDesc: WorkDesc, attributo: String, value: Double)
  case class WorkerGet(workerDesc: WorkDesc)
  
  // Messages from Master to Client
  case class MasterAlarm(worker: ActorRef, attributo: String, value: Double)
  case class ResponseGet(worker: ActorRef, attributo: String, value: Double)
  
  // Messages from Workers to Master
  case class WorkerRegister(workerDesc: WorkDesc)  //fatto
  case class WorkerAlarm(workerDesc: WorkDesc)  //fatto controllare
  case class WorkerStatus(workerDesc: WorkDesc)   
  case class WorkerDone(workerDesc: WorkDesc, attributo: String, printState:() => Unit)
 
  // Messages Master to Workers
  case class WorkerGetStatus()  //fatto
  case class WorkerSetStatus(attributo: String, value: Double)
  case class WorkerStop()
  case class WorkerLow()
  case class WorkerHigh()
  case class Terminated(id: ActorRef)
  
  // master to termostato
  case class TermostatoSetTemperatura(value: Double)
  case class TermostatoStart()
  
  // Master to Forno
  case class FornoStart()
  
  // Master to Lavatrice
  case class LavatriceSetLavaggio(value: String)
  

}

