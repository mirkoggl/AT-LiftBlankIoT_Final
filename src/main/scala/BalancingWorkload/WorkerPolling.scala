package BalancingWorkload

import akka.actor.ActorPath
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef

class WorkerPolling(masterLocation: ActorPath)
  extends Actor with ActorLogging {
  import MasterWorkerProtocol._
  
  // We need to know where the master is
  val master = context.actorSelection(masterLocation)

  def idle: Receive = {
    // Master says there's work to be done, let's ask for it
    case Poll =>
        while(true){
        master ! Poll
        Thread.sleep(5000)
      }
  }
 
  def receive = idle
}