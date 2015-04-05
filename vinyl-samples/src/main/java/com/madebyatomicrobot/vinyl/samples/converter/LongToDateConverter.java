package com.madebyatomicrobot.vinyl.samples.converter;

import java.util.Date;

public class LongToDateConverter {
    public Date convertFrom(Long value) {
        return (value == null) ? null : new Date(value);
    }

    public Long convertTo(Date value) {
        return (value == null) ? null : value.getTime();
    }
}
