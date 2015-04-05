package com.madebyatomicrobot.vinyl.samples.singleparent;

import com.madebyatomicrobot.vinyl.annotations.Record;

// Inherited interfaces that are marked with @Record will also have their own implementation generated
@Record
public interface Parent {
    String parent();
}
