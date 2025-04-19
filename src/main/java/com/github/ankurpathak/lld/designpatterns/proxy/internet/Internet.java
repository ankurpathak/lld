package com.github.ankurpathak.lld.designpatterns.proxy.internet;

import java.util.Set;

interface IInternet {
    void connectTo(String url) throws Exception;
}

class RealInternet implements IInternet {
    @Override
    public void connectTo(String url) {
        System.out.println("Connecting to " + url);
    }
}

class ProxyInternet implements IInternet {
    private final RealInternet realInternet = new RealInternet();

    private static final Set<String> bannedSites = Set.of(
            "facebook.com",
            "twitter.com",
            "instagram.com",
            "youtube.com",
            "tiktok.com",
            "x.com"
    );

    @Override
    public void connectTo(String url) throws Exception {
        for (String site : bannedSites) {
            if (url.contains(site)) {
                throw new Exception("Access Denied to " + url);
            }
        }
        realInternet.connectTo(url);
    }
}

class Main {
    public static void main(String[] args) {
        IInternet internet = new ProxyInternet();
        try {
            internet.connectTo("google.com");
            internet.connectTo("facebook.com");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}