import BalancingWorkload._

class MainCambia extends Runnable{
  
  def run = {
    while(true){
        Thread sleep 5000
        MasterObj.cambia
        println("[CAMBIATO]")
    }
  }
}