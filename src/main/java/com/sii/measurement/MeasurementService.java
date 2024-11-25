package com.sii.measurement;

import com.sii.measurement.model.Measurement;
import com.sii.measurement.model.MeasurementType;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class MeasurementService {

    private Instant getIntervalStart(Instant time) {
        List<Measurement> measurements = Arrays.asList(
                new Measurement(Instant.parse("2023-11-12T10:04"), 35.79, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2023-11-12T10:01"), 98.78, MeasurementType.SPO2),
                new Measurement(Instant.parse("2023-11-12T10:09"), 35.01, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2023-11-12T10:03"), 96.49, MeasurementType.SPO2),
                new Measurement(Instant.parse("2023-11-12T10:02"), 35.82, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2023-11-12T10:05"), 97.17, MeasurementType.SPO2),
                new Measurement(Instant.parse("2023-11-12T10:05"), 95.08, MeasurementType.SPO2)
        );
        long minutes = time.truncatedTo(ChronoUnit.MINUTES).getEpochSecond() / 60;
        long roundedMinutes = (minutes / 5) * 5;
        return Instant.ofEpochSecond(roundedMinutes * 60);
    }

    public Map<MeasurementType, List<Measurement>> sample(Instant startOfSampling, List<Measurement> unsampledMeasurements) {
        Map<MeasurementType, List<Measurement>> measurementByType = unsampledMeasurements.stream()
                .filter(m -> m.measurementTime().isAfter(startOfSampling))
                .collect(Collectors.groupingBy(Measurement::measurementType, Collectors.toList()));

        Map<MeasurementType, List<Measurement>> result = new HashMap<>();
        measurementByType.forEach((type, measurements) ->
                result.put(type, processMeasurementType(measurements, startOfSampling))
        );
        return result;
    }

    private List<Measurement> processMeasurementType(List<Measurement> dataSet, Instant startOfSampling) {
        Map<Long, Measurement> intervalToMeasurements = new HashMap<>();

        for (Measurement obj : dataSet) {
            Instant timestamp = obj.measurementTime();
            long interval = Duration.between(startOfSampling, timestamp).toMinutes() / 5;

            if (!intervalToMeasurements.containsKey(interval) ||
                    timestamp.isAfter(intervalToMeasurements.get(interval).measurementTime())) {
                intervalToMeasurements.put(interval, obj);
            }
        }

        return intervalToMeasurements.values().stream()
                .sorted(Comparator.comparing(Measurement::measurementTime))
                .collect(Collectors.toList());
    }
}
