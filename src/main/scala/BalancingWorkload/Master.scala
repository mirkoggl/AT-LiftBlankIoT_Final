package BalancingWorkload

import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.pipe
import akka.pattern.ask
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorLogging
import java.util.concurrent.TimeUnit
import scala.collection.mutable.MutableList

class Master extends Actor with ActorLogging {
  import MasterWorkerProtocol._
  import scala.collection.mutable.Map

  var workers : MutableList[WorkDesc] = MutableList()
  var powerTot : Double = 0
  
  def checkTempAmb(stato : WorkDesc) : Unit = {
    println(stato.id)
    println(stato.status.temp)
    if(stato.status.temp < 10 ||stato.status.temp > 30){
      alarm("Temperatura ambiente fuori range")
      stato.id ! TermostatoSetTemperatura(25)
      
     println("Temperatura ambiente fuori range chiedo di abbassare")
     implicit val timeout = Timeout(6, TimeUnit.SECONDS)                  
     val future = stato.id ? WorkerGetStatus         
     val result = Await.result(future, timeout.duration).asInstanceOf[WorkDescSup]  
    
     if(result.wd.status.temp > 100){
       println("Temperatura ancora alta, spengo il componente")
       stato.id ! WorkerSetStatus("levelPower", OFF)
     }
    }
    else println("[Master] Temperatura ambientale nella norma")
  }
  
  def checkPower(stato : WorkDesc) : Unit = {
    var pot = stato.status.power
    powerTot += pot
    if(pot > 800){
      
     stato.id !  WorkerSetStatus("levelPower", LOWPOWER)
      
     println("Potenza troppo alta chiedo di abbassare")
     implicit val timeout = Timeout(6, TimeUnit.SECONDS)                  
     val future = stato.id ? WorkerGetStatus // impongo modalità green energy          
     val result = Await.result(future, timeout.duration).asInstanceOf[WorkDescSup]  
     
     if(result.wd.status.power > 800)
       println("Potenza invariata")
        stato.id !  WorkerSetStatus("levelPower", OFF)
    }
    else if (powerTot > 1500){
      alarm("Esubero potenza massima, spengo " + stato.id + "/" + stato.desc)
      stato.id !  WorkerSetStatus("levelPower", OFF)
    }
  }
  
  def alarm(s : String) : Unit = {
    log.error(s)
  } 
  
  def printWorkers = {
    if (workers.nonEmpty){
      println("[Master] Worker presenti: ")
      var i = 0
      workers.foreach{
        w =>
          println("      [" + i + "]" + w.id + ": " + w.desc + " ")
          i = i + 1
      }
      println(" ")
    }
  }
  
  def pollingWorkers() : Unit = {
    powerTot = 0
    var termo = for(i <- workers if i.desc == "Termostato") yield i
   // println("il worker : " + termo(0). + " è un : " + termo(0).desc)
    if(termo.nonEmpty){
      println("\n\n[MASTER] CHECK TEMPERATURA")
      checkTempAmb(termo(0))
      println("\n\n[MASTER] CHECK POTENZA")
      workers.foreach { 
        worker =>//  case (worker, m) => 
          implicit val timeout = Timeout(5,TimeUnit.SECONDS)                  
          val future = worker.id ? WorkerGetStatus // enabled by the “ask” import         
          val result = Await.result(future, timeout.duration).asInstanceOf[WorkDescSup]                   
          println("L'oggetto : " + result.wd.desc + " ha stato :") 
          result.printState()
          checkPower(result.wd)
        }
      }
    else println("[Master] Termostato non trovato ")
  }
 
  def receive = {
    // Worker is alive. Add him to the list, watch him for
    // death, and let him know if there's work to be done
  case Poll =>
    pollingWorkers()
  
  case WorkerRegister(workerDesc) =>
      log.info("Worker registrato: {}", workerDesc.desc)
      context.watch(workerDesc.id)
      workers +:= workerDesc
 
    // A worker wants more work.  If we know about him, he's not
    // currently doing anything, and we've got something to do,
    // give it to him.
    case WorkerAlarm(workerDesc) =>
      log.info("Worker alarm {}", workerDesc.id)
      //alarm to client
      //controlla
  
    case WorkerGet(workerDesc) =>
      var find = for(i <- workers if(workerDesc.id == i.id)) yield i
      if(find.nonEmpty){
        implicit val timeout = Timeout(5,TimeUnit.SECONDS)                  
        val future = find(0).id ? WorkerGetStatus // enabled by the “ask” import         
        val result = Await.result(future, timeout.duration).asInstanceOf[WorkDescSup]                   
        println("[Master] Stato di " + result.wd.desc)
        result.printState()
        sender ! result
      }
     
    //Richiamata a valle di una WorkerSet  
    case WorkerDone(workerDesc, attributo, printState) =>
      var find = for(i <- workers if i.id == workerDesc.id) yield i
      println("[Master] Il worker : "+ workerDesc.desc + " ha settato l'attributo : " + attributo)
      if(find.nonEmpty){
    //    workers = for(i <- workers if i.id != workerDesc.id) yield i
    //    workers +:= workerDesc
        workers(workers.indexOf(find(0))) = workers(workers.indexOf(find(0))).copy( status = workerDesc.status )
        printState()
      }
      
    case WorkerSet(worker, attributo, value) =>
      var find = for(i <- workers if(worker.id == i.id))yield i
      if(find.nonEmpty){
        find(0).id ! WorkerSetStatus(attributo, value)
       }
      
    case FornoStart =>
      var find = for(i <- workers if i.desc == "Forno") yield i.id
      if(find.nonEmpty)
        find(0) ! FornoStart
      else
        println("[Master] Forno non trovato")
        
     case TermostatoStart =>
      var find = for(i <- workers if i.desc == "Termostato") yield i.id
      if(find.nonEmpty)
        find(0) ! TermostatoStart
      else
        println("[Master] Termostato non trovato")
        
     case LavatriceSetLavaggio(value) =>
      var find = for(i <- workers if i.desc == "Lavatrice") yield i.id
      if(find.nonEmpty)
        find(0) ! LavatriceSetLavaggio(value)
      else
        println("[Master] Lavatrice non trovata")
      
    // A worker died.  If he was doing anything then we need
    // to give it to someone else so we just add it back to the
    // master and let things progress as usual
    case Terminated(worker) =>
      printWorkers
      var find = for(i <- workers if i.id == worker) yield i
      if (find.nonEmpty) {                  
        find(0).id ! WorkerStop
        println("[Master] Worker " + find(0).desc + " eliminato dalla lista.")
        workers = for(i <- workers if i.id != worker) yield i
        printWorkers
      }
      else 
        println("[Master] Worker non trovato in lista.")
        
    case GetWorkerId =>
      sender ! workers
  }
}