package com.github.ankurpathak.lld.designpatterns.state.vendingmachine;

import lombok.Setter;

import java.util.Collections;
import java.util.List;

interface State {
    default void pressInsertCoin() {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default void pressSelectItem() {
        throw new UnsupportedOperationException("Insert coin button not supported in this state");
    }


    default void insertCoin(Coin coin) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default void selectItem(int code) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default List<Coin> cancel() {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }



    default List<Coin> returnChange() {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default Item dispenseItem() {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default void loadItem(Item item, int code) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default void loadCoins(Coin coin, int quantity) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    private void notSupported() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

}

class Coin { }

class Item { }



@Setter
class VendingMachine implements State {

    public VendingMachine(){
        this.currentState = new IdealState(this);
    }


    private State currentState;

    @Override
    public void pressInsertCoin() {
        currentState.pressInsertCoin();
    }

    @Override
    public void pressSelectItem() {
        currentState.pressInsertCoin();
    }

    @Override
    public void insertCoin(Coin coin) {
        currentState.pressInsertCoin();
    }

    @Override
    public void selectItem(int code) {
        currentState.pressInsertCoin();
    }

    @Override
    public List<Coin> cancel() {
        return currentState.cancel();
    }

    @Override
    public List<Coin> returnChange() {
        return currentState.returnChange();
    }

    @Override
    public Item dispenseItem() {
        return currentState.dispenseItem();
    }

    @Override
    public void loadItem(Item item, int code) {
        currentState.loadItem(item, code);
    }

    @Override
    public void loadCoins(Coin coin, int quantity) {
        currentState.loadCoins(coin, quantity);
    }


}

class IdealState implements State {
    private final VendingMachine vendingMachine;

    public IdealState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void pressInsertCoin() {
        vendingMachine.setCurrentState(new HasMoneyState(vendingMachine));
    }

    @Override
    public void loadItem(Item item, int code) {

    }

    @Override
    public void loadCoins(Coin coin, int quantity) {

    }
}

class HasMoneyState implements State {
    private final VendingMachine vendingMachine;

    public HasMoneyState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void pressSelectItem() {
        vendingMachine.setCurrentState(new SelectItemState(vendingMachine));
    }

    @Override
    public void insertCoin(Coin coin) {}

    @Override
    public List<Coin> cancel() {
        vendingMachine.setCurrentState(new IdealState(vendingMachine));
        return Collections.emptyList();
    }
}

class SelectItemState implements State {
    private final VendingMachine vendingMachine;

    public SelectItemState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectItem(int code) {
        returnChange();
    }

    public List<Coin> returnChange() {
        vendingMachine.setCurrentState(new IdealState(vendingMachine));
        return Collections.emptyList();
    }

    @Override
    public List<Coin> cancel() {
        vendingMachine.setCurrentState(new IdealState(vendingMachine));
        return Collections.emptyList();
    }
}

class DispenseItemState implements State {
    private final VendingMachine vendingMachine;

    public DispenseItemState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public Item dispenseItem() {
        vendingMachine.setCurrentState(new IdealState(vendingMachine));
        return new Item();
    }
}