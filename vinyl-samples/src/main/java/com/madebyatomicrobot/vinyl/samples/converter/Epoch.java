package com.madebyatomicrobot.vinyl.samples.converter;

import com.madebyatomicrobot.vinyl.annotations.Converter;
import com.madebyatomicrobot.vinyl.annotations.Record;

import java.util.Date;

@Record
public interface Epoch {
    @Converter(fieldClass = Long.class, converter = LongToDateConverter.class)
    Date time();
}
