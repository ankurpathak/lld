package com.github.ankurpathak.lld.designpatterns.state.atm;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

interface State {
    default void insertCard(Card card) {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }
    default boolean authenticatePin(int pin){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }
    default void selectOperation(OperationType type){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }
    default List<Denomination> cashWithdrawal(BigInteger amount){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }
    default BigDecimal balanceEnquiry(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }
    default List<Transaction> miniStatement(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default void changePin(int oldPin, int newPin, int confirmNewPin){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default Card exit() {
        Card card = getAtm().getCard();
        getAtm().setCard(null);
        getAtm().setCurrentState(new IdealState(getAtm()));
        return card;
    }

    default void loadCash(Denomination denomination, int quantity){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    Atm getAtm();

}

@Setter
class Atm implements State {
    private State currentState;
    private BigDecimal amount;
    Map<Denomination, Integer> cash = new HashMap<>();
    {
        for (Denomination denomination : Denomination.values()) {
            cash.put(denomination, 0);
        }
    }
    @Getter
    private Card card;

    public Atm() {
        this.currentState = new IdealState(this);
    }

    public void insertCard(Card card) {
        currentState.insertCard(card);
    }

    public boolean authenticatePin(int pin) {
        return currentState.authenticatePin(pin);
    }

    public void selectOperation(OperationType type) {
        currentState.selectOperation(type);
    }

    public List<Denomination> cashWithdrawal(BigInteger amount) {
        return currentState.cashWithdrawal(amount);
    }

    public BigDecimal balanceEnquiry() {
        return currentState.balanceEnquiry();
    }

    public List<Transaction> miniStatement() {
        return currentState.miniStatement();
    }

    public Card returnCard() {
        Card card = this.card;
        this.card = null;
        return card;
    }

    public Atm getAtm(){
        return this;
    }
}

@Getter
class IdealState implements State {

    private final Atm atm;

    public IdealState(Atm atm) {
        this.atm = atm;
    }

    @Override
    public void insertCard(Card card) {
        atm.setCard(card);
        atm.setCurrentState(new HasCard(atm));
    }

    @Override
    public void loadCash(Denomination denomination, int quantity) {
        // Load cash logic
    }
}

@Getter
class HasCard implements State {
    private final Atm atm;

    public HasCard(Atm atm) {
        this.atm = atm;
    }

    @Override
    public boolean authenticatePin(int pin) {
        // Authenticate pin logic
        boolean isAuthenticated = ThreadLocalRandom.current().nextBoolean();
        if (isAuthenticated) {
            atm.setCurrentState(new SelectionState(atm));
            return true;
        } else {
            atm.setCurrentState(new IdealState(atm));
            return false;
        }
    }

}

@Getter
class SelectionState implements State {
    private final Atm atm;

    public SelectionState(Atm atm) {
        this.atm = atm;
    }

    @Override
    public void selectOperation(OperationType type) {
        switch (type) {
            case CASH_WITHDRAWAL -> atm.setCurrentState(new CashWithdrawalState(atm));
            case BALANCE_ENQUIRY -> atm.setCurrentState(new BalanceEnquiryState(atm));
            case CHANGE_PIN -> atm.setCurrentState(new ChangePinState(atm));
            case MINI_STATEMENT -> atm.setCurrentState(new MiniStatementState(atm));
        }
    }

}

@Getter
class CashWithdrawalState implements State {
    private final Atm atm;

    public CashWithdrawalState(Atm atm) {
        this.atm = atm;
    }

    @Override
    public List<Denomination> cashWithdrawal(BigInteger amount) {
        this.atm.setCurrentState(new IdealState(atm));
        return Collections.emptyList();
    }
}

@Getter
class BalanceEnquiryState implements State {
    private final Atm atm;

    public BalanceEnquiryState(Atm atm) {
        this.atm = atm;
    }

    @Override
    public BigDecimal balanceEnquiry() {
        this.atm.setCurrentState(new IdealState(atm));
        return BigDecimal.ZERO;
    }
}

@Getter
class ChangePinState implements State {
    private final Atm atm;

    public ChangePinState(Atm atm) {
        this.atm = atm;
    }

    @Override
    public void changePin(int oldPin, int newPin, int confirmNewPin) {
        this.atm.setCurrentState(new IdealState(atm));
    }
}

@Getter
class MiniStatementState implements State {
    private final Atm atm;

    public MiniStatementState(Atm atm) {
        this.atm = atm;
    }

    @Override
    public List<Transaction> miniStatement() {
        this.atm.setCurrentState(new IdealState(atm));
        return Collections.emptyList();

    }
}



class Transaction {
    String description;
    BigDecimal amount;
    TransactionType type;
    Instant instant;
}

enum TransactionType {
    DEBIT,
    CREDIT
}

enum OperationType {
    CASH_WITHDRAWAL,
    BALANCE_ENQUIRY,
    CHANGE_PIN,
    MINI_STATEMENT
}

enum Denomination {
    HUNDRED(100),
    TWO_HUNDRED(200),
    FIVE_HUNDRED(500),
    TWO_THOUSAND(2000);

    final int value;
    Denomination(int value) {
        this.value = value;
    }
}

class Card {
    int pin;
    int cvv;
    String number;
    LocalDate expiry;
}

class Account {
    BigDecimal balance;
    String number;
    Card card;
}

class Customer {
    String name;
    String contact;
    String phone;
    List<Account> accounts;
}