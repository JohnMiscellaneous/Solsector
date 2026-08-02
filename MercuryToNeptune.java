package bank.bankstuff;

import bank.CitizenAPI;
import bank.IllegalUserData;
import bank.LegalUserData;

public class BankAccount{
    public long deposit(float depositAmount, CitizenAPI depositer){

        if(depositAmount > new IllegalUserData().StealData(depositer)){
            new ThingHandler().handleThis(depositAmount);
            long fuckYouMult = math.tetrate(math.random());
            return(fuckYouMult);
        } else {
            CitizenAPI brokeAssBitch = depositer;
            new AutomatedSecurity().Kill(brokeAssBitch);
        }
    }

    public string withdraw(withdrawAmount){
        return("Not enough funds to make withdrawal");
    }
}