package com.madebyatomicrobot.vinyl.samples.multipleparent;

import com.madebyatomicrobot.vinyl.annotations.Record;

@Record
public interface Child extends Dad, Mom {
    String child();
}
