package org.githhub.ankurpathak.lld.designpatterns.chainofresponsibility.atm;

import lombok.Setter;

class AtmDispenser {
    int denomination;
    int count;

    @Setter
    AtmDispenser nextDispenser;

    public AtmDispenser(int denomination, int count) {
        this.denomination = denomination;
        this.count = count;
    }

    public void dispense(int amount) {
        if (amount >= denomination) {
            int numNotes = amount / denomination;
            amount %= denomination;
            if(numNotes > count) {
                numNotes = count;
                count = 0;
                amount = amount + (numNotes * denomination);
            }else {
                count -= numNotes;
            }
            System.out.println("Dispensing " + numNotes + " notes of " + denomination);

        }
        if (nextDispenser != null && amount > 0) {
            nextDispenser.dispense(amount);
        }
    }
}

class Atm extends AtmDispenser {

    public Atm(){
        super(-1, -1);
        this.dispenser = buildChain();
    }

    AtmDispenser dispenser;

    private AtmDispenser buildChain(){
        AtmDispenser dispenser2000 = new AtmDispenser(2000, 1);
        AtmDispenser dispenser500 = new AtmDispenser(500, 1);
        AtmDispenser dispenser200 = new AtmDispenser(200, 20);
        AtmDispenser dispenser100 = new AtmDispenser(100, 1000);

        dispenser2000.setNextDispenser(dispenser500);
        dispenser500.setNextDispenser(dispenser200);
        dispenser200.setNextDispenser(dispenser100);

        return dispenser2000;
    }

    public void dispense(int amount) {
        dispenser.dispense(amount);
    }
}

class Main {
    public static void main(String[] args) {
        Atm atm = new Atm();
        int amount = 1300;
        System.out.println("Requesting amount: " + amount);
        atm.dispense(amount);

    }
}