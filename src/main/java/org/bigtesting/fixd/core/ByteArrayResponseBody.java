package org.bigtesting.fixd.core;

import java.io.ByteArrayInputStream;

public class ByteArrayResponseBody extends InputStreamResponseBody {

    public ByteArrayResponseBody(byte[] content) {
        super(new ByteArrayInputStream(content));
    }
}
