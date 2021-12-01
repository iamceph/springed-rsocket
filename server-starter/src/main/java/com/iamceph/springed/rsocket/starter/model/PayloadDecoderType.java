package com.iamceph.springed.rsocket.starter.model;

import io.rsocket.frame.decoder.PayloadDecoder;

public enum PayloadDecoderType {
    /**
     * DEFAULT PayloadDecoder, defined in {@link PayloadDecoder#DEFAULT}
     */
    DEFAULT,
    /**
     * ZERO_COPY PayloadDecoder, defined in {@link PayloadDecoder#ZERO_COPY}
     */
    ZERO_COPY,
    /**
     * CUSTOM PayloadDecoder, you need to define this one as a Bean.
     */
    CUSTOM
}
