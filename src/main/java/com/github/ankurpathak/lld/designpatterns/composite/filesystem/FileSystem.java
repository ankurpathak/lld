package com.github.ankurpathak.lld.designpatterns.composite.filesystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface IFile {
    void ls();
}

class File implements IFile {
    private final String name;

    public File(String name) {
        this.name = name;
    }

    @Override
    public void ls() {
        System.out.println("File: " + name);
    }
}

class Directory implements IFile {
    private final String name;
    private final List<IFile> files;

    public Directory(String name, IFile... files) {
        this.name = name;
        this.files = new ArrayList<>(Arrays.asList(files));
    }

    public void addFile(IFile file) {
        files.add(file);
    }

    @Override
    public void ls() {
        System.out.println("Directory: " + name);
        for (IFile file : files) {
            file.ls();
        }
    }
}

class Main {
    public static void main(String[] args) {
        IFile file1 = new File("file1.txt");
        IFile file2 = new File("file2.txt");
        IFile file3 = new File("file3.txt");

        Directory dir1 = new Directory("dir1", file1, file2);
        Directory dir2 = new Directory("dir2", file3);

        Directory rootDir = new Directory("root", dir1, dir2);
        rootDir.ls();
    }
}