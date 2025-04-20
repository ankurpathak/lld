package com.github.ankurpathak.lld.designpatterns.state.vendingmachine;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;

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


    default Item dispenseItem(int item) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default void loadItem(Item item, int code) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }


    default void loadCoins(Coin coin, int quantity) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }



}

enum Coin {
    ONE(1),
    TWO(2),
    FIVE(5),
    TEN(10);


    private final int value;

    Coin(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

enum ItemType {
    COKE, PEPSI, JUICE, SODA
}

@RequiredArgsConstructor
class Item {
    final ItemType type;
    final BigDecimal price;
}



class ItemShelf {
    Item item;
    int code;

    public ItemShelf(int code) {
        this.code = code;
    }
}

class Inventory {
   Map<Integer, ItemShelf> itemShelves = new LinkedHashMap<>();

    List<Coin> coins = new ArrayList<>();

    void addItemShelf(int code) {
        itemShelves.put(code, new ItemShelf(code));
    }

    void removeItemShelf(int code) {
        itemShelves.remove(code);
    }

    void assignItem(int code, Item item) {
        if (itemShelves.containsKey(code)) {
            itemShelves.get(code).item = item;
        } else {
            throw new IllegalArgumentException("Item shelf with code " + code + " does not exist");
        }
    }

    Item releaseItem(int code) {
        if (itemShelves.containsKey(code)) {
            Item item = itemShelves.get(code).item;
            itemShelves.get(code).item =null;
            return item;
        } else {
            throw new IllegalArgumentException("Item shelf with code " + code + " does not exist");
        }
    }
}


@Setter
class VendingMachine implements State {

    public VendingMachine(){
        this.currentState = new IdealState(this);
        this.inventory = new Inventory();
    }

    State currentState;
    final Inventory inventory;
    final Map<Coin, Integer> coins = new HashMap<>();
    {
        for(Coin coin : Coin.values()){
            coins.put(coin, 0);
        }
    }

    void addCoin(Coin coin, int quantity) {
        coins.put(coin, coins.get(coin) + quantity);
    }

    void removeCoin(Coin coin, int quantity) {
        coins.put(coin, coins.get(coin) - quantity);
    }

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
    public Item dispenseItem(int code) {
        return currentState.dispenseItem(code);
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
        vendingMachine.setCurrentState(new SelectionState(vendingMachine));
    }

    @Override
    public void insertCoin(Coin coin) {
        vendingMachine.addCoin(coin, 1);
    }

    @Override
    public List<Coin> cancel() {
        vendingMachine.setCurrentState(new IdealState(vendingMachine));
        return Collections.emptyList();
    }
}

class SelectionState implements State {
    private final VendingMachine vendingMachine;

    public SelectionState(VendingMachine vendingMachine) {
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
    public Item dispenseItem(int code) {
        vendingMachine.setCurrentState(new IdealState(vendingMachine));
        return vendingMachine.inventory.releaseItem(code);
    }
}