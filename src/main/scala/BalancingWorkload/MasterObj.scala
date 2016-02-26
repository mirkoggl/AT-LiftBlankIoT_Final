package BalancingWorkload

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor._
import  scala.concurrent.duration._
import akka.util._
import scala.collection.mutable.ListBuffer
import BalancingWorkload.MasterWorkerProtocol.WorkerSet

object MasterObj {
  
  println("Sono in object")
   
   val sdNull = StatusDesc(0, 0, 0, 0, 0, 0, 0)
   val system = ActorSystem("TempSystem")
    
    def workerForno(name: String, desc: String) = system.actorOf(Props(
      
    new Forno(ActorPath.fromString(
      "akka://%s/user/%s".format(system.name, name)), desc)))
      
      
    def workerLavatrice(name: String, desc: String) = system.actorOf(Props(
      
    new Lavatrice(ActorPath.fromString(
      "akka://%s/user/%s".format(system.name, name)), desc)))
      
    def workerPolling(name: String) = system.actorOf(Props(

     new WorkerPolling(ActorPath.fromString(
      "akka://%s/user/%s".format(system.name, name)))))
              
    def workerTermostato(name: String, desc: String) = system.actorOf(Props(
      
      new Termostato(ActorPath.fromString(
       "akka://%s/user/%s".format(system.name, name)), desc)))
      
      
      val m = system.actorOf(Props[Master], "master")
      // Create three workers
      val w1 = workerForno("master", "Forno")
      val w2 = workerLavatrice("master", "Lavatrice")
      val w4 = workerLavatrice("master", "Televisore")
      val w3 = workerTermostato("master", "Termostato")    

      val w2Desc = WorkDesc(w2, "Lavatrice", sdNull)
      val w4Desc = WorkDesc(w4, "Lavatrice2", sdNull)
      val w1Desc = WorkDesc(w1, "Forno", sdNull)
      val w3Desc = WorkDesc(w3, "Termostato", sdNull)
      val wPolling = workerPolling("master")
      
      
      def cambia = {
    
        def randNum = Math.floor((Math.random()*10000))/100 
        
        m ! WorkerSet(w1Desc, "temperatura", randNum)
        m ! WorkerSet(w2Desc, "temperatura", randNum)
       // m ! WorkerSet(w3Desc, "temperatura", randNum)
        m ! WorkerSet(w4Desc, "temperatura", randNum)
        
        m ! WorkerSet(w1Desc, "pressione", randNum)
        m ! WorkerSet(w2Desc, "pressione", randNum)
        m ! WorkerSet(w3Desc, "pressione", randNum)
        m ! WorkerSet(w4Desc, "pressione", randNum)
        
  //      m ! WorkerSet(w1Desc, "potenza", randNum)
  //      m ! WorkerSet(w2Desc, "potenza", randNum)
  //      m ! WorkerSet(w3Desc, "potenza", randNum)
  //      m ! WorkerSet(w4Desc, "potenza", randNum)
  }
  
    def newWorkers = {
  /*    val w1 = workerForno("master", "Forno1")
      val w2 = workerLavatrice("master", "Lavatrice2")
      val w4 = workerLavatrice("master", "Lavatrice3")
      val w3 = workerTermostato("master", "Termostato4")   
    */  
      var i : Int = 0
      
      while(i < 4){
        i += 1
        val x = (Math.floor((Math.random()*100)))%4
        if(x == 1){
           var w1 = workerForno("master", "Forno")
        }
        else if(x == 2){
           val w2 = workerLavatrice("master", "Lavatrice")
        }
        else if(x == 3){
           val w1 = workerForno("master", "Televisore")
        }
        else{
           val w3 = workerTermostato("master", "Termostato")   
        }
        
      }
      
    }
  
}