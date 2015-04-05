package com.madebyatomicrobot.vinyl.samples.projection;

import android.os.Build;

import com.madebyatomicrobot.vinyl.annotations.ConditionalProjection;
import com.madebyatomicrobot.vinyl.annotations.Record;
import com.madebyatomicrobot.vinyl.annotations.Projection;

@Record
public interface ExplicitProjection {
    int implicit();

    @Projection("explicit_field")
    int explicit();

    // Support for things where the projections could vary at runtime (ex: getting a person's name from Android's
    // contact provider pre and post Honeycomb)
    @Projection(conditionalProjection = SampleConditionalProjection.class)
    int conditional();

    class SampleConditionalProjection implements ConditionalProjection {
        @Override
        public String projection() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? "new_field" : "old_field";
        }
    }
}
