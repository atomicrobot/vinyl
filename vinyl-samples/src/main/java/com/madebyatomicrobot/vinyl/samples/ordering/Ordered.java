package com.madebyatomicrobot.vinyl.samples.ordering;

import com.madebyatomicrobot.vinyl.annotations.Record;

@Record(ordered = true)
public interface Ordered {
    int one();
    int two();
    int three();
}
