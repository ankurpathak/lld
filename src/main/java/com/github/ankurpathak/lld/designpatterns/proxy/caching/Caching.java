package com.github.ankurpathak.lld.designpatterns.proxy.caching;

interface IBookParser {
    int getNumberOfPages();
}


class BookParser implements IBookParser {
    int numberOfPages = 0;

    public BookParser(String text) {
        numberOfPages = text.split("\n").length;
    }

    @Override
    public int getNumberOfPages() {
        return numberOfPages;
    }
}

class LazyBookParser implements IBookParser {
    private BookParser bookParser;
    private final String text;

    public LazyBookParser(String text) {
        this.text = text;
    }

    @Override
    public int getNumberOfPages() {
        if (bookParser == null) {
            bookParser = new BookParser(text);
        }
        return bookParser.getNumberOfPages();
    }
}

class Main {
    public static void main(String[] args) {
        String text = "Page 1\nPage 2\nPage 3\nPage 4\nPage 5\nPage 6\nPage 7\nPage 8\nPage 9\nPage 10";
        IBookParser bookParser = new LazyBookParser(text);
        System.out.println("Number of pages: " + bookParser.getNumberOfPages());
    }
}