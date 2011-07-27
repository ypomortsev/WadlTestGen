package com.pomortsev.wadltestgen.proxy;

enum ConnState {
    IDLE,
    CONNECTED,
    REQUEST_RECEIVED,
    REQUEST_SENT,
    REQUEST_BODY_STREAM,
    REQUEST_BODY_DONE,
    RESPONSE_RECEIVED,
    RESPONSE_SENT,
    RESPONSE_BODY_STREAM,
    RESPONSE_BODY_DONE,
    CLOSING,
    CLOSED
}
