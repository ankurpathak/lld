package com.github.ankurpathak.lld.designpatterns.state.tv;

import lombok.Setter;

interface State {
    default void pressPower() {
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default void pressVolumeUp(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default void pressVolumeDown(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default void pressChannelUp(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default void pressChannelDown(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }

    default void pressMute(){
        throw new UnsupportedOperationException("Operation not supported in this state");
    }
}

@Setter
class Tv implements State {
    private State currentState;

    public Tv() {
        this.currentState = new OffState(this);
    }

    public void pressPower() {
        currentState.pressPower();
    }

    public void pressVolumeUp() {
        currentState.pressVolumeUp();
    }

    public void pressVolumeDown() {
        currentState.pressVolumeDown();
    }

    public void pressChannelUp() {
        currentState.pressChannelUp();
    }

    public void pressChannelDown() {
        currentState.pressChannelDown();
    }
}

class OffState implements State {
    private final Tv tv;

    public OffState(Tv tv) {
        this.tv = tv;
    }

    @Override
    public void pressPower() {
        tv.setCurrentState(new OnState(tv));
    }
}

class OnState implements State {
    private final Tv tv;

    public OnState(Tv tv) {
        this.tv = tv;
    }

    @Override
    public void pressVolumeUp() {

    }

    @Override
    public void pressVolumeDown() {

    }

    @Override
    public void pressChannelUp() {

    }

    @Override
    public void pressChannelDown() {

    }

    @Override
    public void pressMute() {

    }

    @Override
    public void pressPower() {
        tv.setCurrentState(new OffState(tv));
    }


}