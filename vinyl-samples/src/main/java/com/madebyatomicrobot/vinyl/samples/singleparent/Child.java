package com.madebyatomicrobot.vinyl.samples.singleparent;

import com.madebyatomicrobot.vinyl.annotations.Record;

@Record
public interface Child extends Parent {
    String child();
}
