package com.pingine.fleetpulse.service.trip;

import com.pingine.fleetpulse.domain.Trip;
import com.pingine.fleetpulse.persistence.mongo.TelemetryPoint;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Splits a stream of telemetry points into completed trips.
 * A trip starts on ignition=true and ends on the next ignition=false.
 */
@Component
public class TripDetector {

    public List<Trip> detect(List<TelemetryPoint> points) {
        if (points == null || points.isEmpty()) {
            return List.of();
        }

        List<TelemetryPoint> sortedPoints = points.stream()
                .sorted((p1, p2) -> p1.getTs().compareTo(p2.getTs()))
                .collect(Collectors.toList());

        Map<LocalDateTime, TelemetryPoint> uniqueByTime = sortedPoints.stream()
                .collect(Collectors.toMap(
                        TelemetryPoint::getTs,
                        p -> p,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<TelemetryPoint> uniquePoints = new ArrayList<>(uniqueByTime.values());

        List<Trip> trips = new ArrayList<>();
        List<TelemetryPoint> currentTripPoints = null;
        boolean inTrip = false;

        for (int i = 0; i < uniquePoints.size(); i++) {
            TelemetryPoint point = uniquePoints.get(i);

            if (point.isIgnition() && !inTrip) {
                // Начинаем новую поездку
                currentTripPoints = new ArrayList<>();
                currentTripPoints.add(point);
                inTrip = true;
            } else if (!point.isIgnition() && inTrip) {
                // Завершаем текущую поездку
                currentTripPoints.add(point);

                Trip trip = buildTripFromPoints(currentTripPoints);
                if (trip != null) {
                    trips.add(trip);
                }

                inTrip = false;
                currentTripPoints = null;
            } else if (inTrip) {
                // Продолжаем текущую поездку
                currentTripPoints.add(point);
            }
        }

        return trips;
    }

    private Trip buildTripFromPoints(List<TelemetryPoint> points) {
        if (points == null || points.size() < 2) {
            return null;
        }

        String vehicleId = points.get(0).getVehicleId();
        Instant startedAt = points.get(0).getTs().atZone(ZoneOffset.UTC).toInstant();
        Instant endedAt = points.get(points.size() - 1).getTs().atZone(ZoneOffset.UTC).toInstant();

        double totalDistanceKm = 0.0;
        double totalSpeedSum = 0.0;
        int pointsWithSpeed = 0;

        List<Trip.TripPoint> tripPoints = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {
            TelemetryPoint p = points.get(i);

            Trip.TripPoint tripPoint = Trip.TripPoint.builder()
                    .ts(p.getTs().atZone(ZoneOffset.UTC).toInstant())
                    .lat(p.getLat())
                    .lon(p.getLon())
                    .speedKph(p.getSpeed())
                    .build();
            tripPoints.add(tripPoint);

            totalSpeedSum += p.getSpeed();
            pointsWithSpeed++;

            if (i < points.size() - 1) {
                TelemetryPoint next = points.get(i + 1);
                double distance = GeoDistance.haversineKm(p.getLat(), p.getLon(), next.getLat(), next.getLon());
                totalDistanceKm += distance;
            }
        }

        double avgSpeedKph = pointsWithSpeed > 0 ? totalSpeedSum / pointsWithSpeed : 0.0;

        return Trip.builder()
                .vehicleId(vehicleId)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .distanceKm(totalDistanceKm)
                .avgSpeedKph(avgSpeedKph)
                .points(tripPoints)
                .build();
    }
}
