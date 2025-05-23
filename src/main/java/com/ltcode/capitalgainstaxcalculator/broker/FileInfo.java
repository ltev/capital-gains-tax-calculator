package com.ltcode.capitalgainstaxcalculator.broker;

import java.nio.file.Path;

public record FileInfo(
        Broker broker,
        FileType fileType,
        Path path) {

    public boolean isBrokerAndFileTypeSame(FileInfo other) {
        return broker == other.broker && fileType == other.fileType;
    }
}
