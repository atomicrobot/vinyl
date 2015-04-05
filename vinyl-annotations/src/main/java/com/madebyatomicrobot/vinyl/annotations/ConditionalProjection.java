package com.madebyatomicrobot.vinyl.annotations;

/**
 * @see Projection
 */
public interface ConditionalProjection {
    /**
     * Performs runtime evaluation to return a projection for a field.
     *
     * @return evaluated projection for a field.
     */
    String projection();
}
