package com.github.ankurpathak.lld.designpatterns.composite.todo;

import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface IToDo {
    String toHtml();
}


class ToDo implements IToDo {
    private final String text;
    public ToDo(String text){
        this.text = text;
    }

    @Override
    public String toHtml() {
        return text;
    }
}


class ToDoList implements IToDo {

    private final String name;
    private final List<IToDo> toDos;

    public ToDoList(String name, IToDo...toDos) {
        this.name = name;
        this.toDos = new ArrayList<>(Arrays.asList(toDos));
    }

    @Override
    public String toHtml() {
        StringBuilder html = new StringBuilder();
        html.append(name);
        html.append("<ul>");
        for(IToDo toDo: toDos){
            html.append("<li>");
            html.append(toDo.toHtml());
            html.append("</li>");
        }
        html.append("</ul>");

        return html.toString();
    }
}

class Main {
    public static void main(String[] args) {
        ToDo a = new ToDo("A");
        ToDo b = new ToDo("B");
        ToDo c = new ToDo("C");
        ToDo d = new ToDo("D");
        ToDoList lessImp = new ToDoList("Less Important", c, d);
        ToDoList imp = new ToDoList("Important", a, b, lessImp);
        System.out.println(imp.toHtml());
    }
}
