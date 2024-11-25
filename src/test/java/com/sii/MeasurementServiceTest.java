package com.sii;

import com.sii.measurement.model.Measurement;
import com.sii.measurement.MeasurementService;
import com.sii.measurement.model.MeasurementType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeasurementServiceTest {

    private final MeasurementService measurementService = new MeasurementService();

    @Test
    public void testIfFilterIsWorkingProperly() {
        // Given: one measurement is before startOfSampling
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2024-11-18T09:59:00Z"), 10.0, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2024-11-18T10:01:00Z"), 12.0, MeasurementType.TEMPERATURE)
        );

        // When: Sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: measurements after  startOfSampling are included
        assertEquals(1, result.get(MeasurementType.TEMPERATURE).size());
        assertEquals(Instant.parse("2024-11-18T10:01:00Z"), result.get(MeasurementType.TEMPERATURE).get(0).measurementTime());
    }

    @Test
    void testGroupingByMeasurementType() {
        // Given: Measurements with different types
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2024-11-18T10:01:00Z"), 22.0, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2024-11-18T10:02:00Z"), 123.0, MeasurementType.HEART_RATE)
        );

        // When: Sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: Grouped by type
        assertEquals(1, result.get(MeasurementType.TEMPERATURE).size());
        assertEquals(1, result.get(MeasurementType.HEART_RATE).size());
    }


    @Test
    void testSampleFiltersAndSortsCorrectly() {
        // Given: Task example test
        Instant startOfSampling = Instant.parse("2023-11-12T10:00:01Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2023-11-12T10:04:45Z"), 35.79, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2023-11-12T10:01:18Z"), 98.78, MeasurementType.SPO2),
                new Measurement(Instant.parse("2023-11-12T10:09:07Z"), 35.01, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2023-11-12T10:03:34Z"), 96.49, MeasurementType.SPO2),
                new Measurement(Instant.parse("2023-11-12T10:02:01Z"), 35.82, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2023-11-12T10:05:00Z"), 97.17, MeasurementType.SPO2),
                new Measurement(Instant.parse("2023-11-12T10:05:01Z"), 95.08, MeasurementType.SPO2)
        );

        // When: Sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: expected result
        List<Measurement> temperatureMeasurements = result.get(MeasurementType.TEMPERATURE);
        List<Measurement> sp02Measurements = result.get(MeasurementType.SPO2);

        assertEquals(2, temperatureMeasurements.size());
        assertEquals(2, sp02Measurements.size());
        assertEquals(new Measurement(Instant.parse("2023-11-12T10:04:45Z"), 35.79, MeasurementType.TEMPERATURE), temperatureMeasurements.get(0));
        assertEquals(new Measurement(Instant.parse("2023-11-12T10:09:07Z"), 35.01, MeasurementType.TEMPERATURE), temperatureMeasurements.get(1));
        assertEquals(new Measurement(Instant.parse("2023-11-12T10:05:00Z"), 97.17, MeasurementType.SPO2), sp02Measurements.get(0));
        assertEquals(new Measurement(Instant.parse("2023-11-12T10:05:01Z"), 95.08, MeasurementType.SPO2), sp02Measurements.get(1));
    }

    @Test
    void testSampleHandlesEmptyDataSet() {
        // Given: empty data set
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of();

        // When: Sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: empty result
        assertTrue(result.isEmpty());
    }

    @Test
    void testSampleHandlesSingleValidMeasurement() {
        // Given: one valid measurement
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2024-11-18T10:01:00Z"), 1.0, MeasurementType.TEMPERATURE)
        );

        // When
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: result has one measurement
        List<Measurement> temperatureMeasurements = result.get(MeasurementType.TEMPERATURE);
        assertEquals(1, temperatureMeasurements.size());
        assertEquals(new Measurement(Instant.parse("2024-11-18T10:01:00Z"), 1.0, MeasurementType.TEMPERATURE), temperatureMeasurements.get(0));
    }

    @Test
    void testSampleSelectsLatestInEachInterval() {
        // Given: measurements in two separate intervals
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2024-11-18T10:01:02Z"), 1.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:01:03Z"), 12.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:01:04Z"), 3.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:01:05Z"), 44.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:11:02Z"), 1.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:11:03Z"), 12.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:11:04Z"), 3.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:11:05Z"), 44.0, MeasurementType.HEART_RATE)
        );

        // When: sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: only one in each interval is selected
        List<Measurement> heartRateMeasurements = result.get(MeasurementType.HEART_RATE);
        assertEquals(2, heartRateMeasurements.size());
        assertEquals(new Measurement(Instant.parse("2024-11-18T10:01:05Z"), 44.0, MeasurementType.HEART_RATE), heartRateMeasurements.get(0));
        assertEquals(new Measurement(Instant.parse("2024-11-18T10:11:05Z"), 44.0, MeasurementType.HEART_RATE), heartRateMeasurements.get(1));
    }

    @Test
    void testSameInstantMeasurements() {
        // Given: two equal measurements
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2024-11-18T10:01:05Z"), 1.0, MeasurementType.TEMPERATURE),
                new Measurement(Instant.parse("2024-11-18T10:01:05Z"), 1.0, MeasurementType.TEMPERATURE)
        );

        // When: sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: one measurement is present
        List<Measurement> temperatureMeasurements = result.get(MeasurementType.TEMPERATURE);
        assertEquals(1, temperatureMeasurements.size());
        assertEquals(new Measurement(Instant.parse("2024-11-18T10:01:05Z"), 1.0, MeasurementType.TEMPERATURE), temperatureMeasurements.get(0));
    }

    @Test
    void testSamplingResultIsSorted() {
        // Given: measurements in separate intervals
        Instant startOfSampling = Instant.parse("2024-11-18T10:00:00Z");
        List<Measurement> unsampledMeasurements = List.of(
                new Measurement(Instant.parse("2024-11-18T10:41:02Z"), 1.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:31:03Z"), 12.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:21:04Z"), 3.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:11:05Z"), 44.0, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2024-11-18T10:51:02Z"), 1.0, MeasurementType.SPO2),
                new Measurement(Instant.parse("2024-11-18T10:41:03Z"), 12.0, MeasurementType.SPO2),
                new Measurement(Instant.parse("2024-11-18T10:31:04Z"), 3.0, MeasurementType.SPO2),
                new Measurement(Instant.parse("2024-11-18T10:21:05Z"), 44.0, MeasurementType.SPO2)
        );

        // When: sampling
        Map<MeasurementType, List<Measurement>> result = measurementService.sample(startOfSampling, unsampledMeasurements);

        // Then: sorted list of results appears for each measurement type
        List<Measurement> heartRateMeasurements = result.get(MeasurementType.HEART_RATE);
        List<Measurement> sp02Measurements = result.get(MeasurementType.SPO2);
        assertEquals(Instant.parse("2024-11-18T10:11:05Z"), heartRateMeasurements.get(0).measurementTime());
        assertEquals(Instant.parse("2024-11-18T10:21:04Z"), heartRateMeasurements.get(1).measurementTime());
        assertEquals(Instant.parse("2024-11-18T10:31:03Z"), heartRateMeasurements.get(2).measurementTime());
        assertEquals(Instant.parse("2024-11-18T10:41:02Z"), heartRateMeasurements.get(3).measurementTime());

        assertEquals(Instant.parse("2024-11-18T10:21:05Z"), sp02Measurements.get(0).measurementTime());
        assertEquals(Instant.parse("2024-11-18T10:31:04Z"), sp02Measurements.get(1).measurementTime());
        assertEquals(Instant.parse("2024-11-18T10:41:03Z"), sp02Measurements.get(2).measurementTime());
        assertEquals(Instant.parse("2024-11-18T10:51:02Z"), sp02Measurements.get(3).measurementTime());

    }
}