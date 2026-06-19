package com.reader.common;

public enum ErrorCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_ERROR(500),
    SOURCE_NOT_FOUND(1001),
    COMIC_NOT_FOUND(1002),
    TAG_NOT_FOUND(1003),
    SCAN_ERROR(1004),
    FILE_NOT_FOUND(1005),
    PARSE_ERROR(1006);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
