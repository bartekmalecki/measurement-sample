package com.sii.measurement.model;

import java.time.Instant;

public record Measurement( Instant measurementTime, Double measurementValue, MeasurementType measurementType) {}

