package com.ltcode.capitalgainstaxcalculator.broker;

import java.nio.file.Path;

public class FileInfo {

    private Broker broker;
    private FileType fileType;
    private Path path;

    public FileInfo(Broker broker, FileType fileType, Path path) {
        this.broker = broker;
        this.fileType = fileType;
        this.path = path;
    }

    public Broker getBroker() {
        return broker;
    }

    public FileType getFileType() {
        return fileType;
    }

    public Path getPath() {
        return path;
    }
}
