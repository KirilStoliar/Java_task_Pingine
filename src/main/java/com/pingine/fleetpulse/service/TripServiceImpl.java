package com.pingine.fleetpulse.service;

import com.pingine.fleetpulse.api.dto.TripResponse;
import com.pingine.fleetpulse.api.dto.VehicleResponse;
import com.pingine.fleetpulse.domain.Trip;
import com.pingine.fleetpulse.persistence.mongo.TelemetryPoint;
import com.pingine.fleetpulse.persistence.mongo.TelemetryRepository;
import com.pingine.fleetpulse.service.trip.TripDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TelemetryRepository telemetryRepository;
    private final TripDetector tripDetector;
    private final VehicleService vehicleService;

    @Override
    public TripResponse getLastTrip(String vehicleId) {
        log.debug("Getting last trip for vehicle: {}", vehicleId);

        VehicleResponse vehicle = vehicleService.getById(vehicleId);

        List<TelemetryPoint> telemetryPoints = telemetryRepository.findByVehicleIdOrderByTsAsc(vehicleId);

        if (telemetryPoints.isEmpty()) {
            log.warn("No telemetry points found for vehicle: {}", vehicleId);
            throw new VehicleNotFoundException(vehicleId);
        }

        List<Trip> trips = tripDetector.detect(telemetryPoints);

        if (trips.isEmpty()) {
            log.warn("No completed trips found for vehicle: {}", vehicleId);
            throw new VehicleNotFoundException(vehicleId);
        }

        Trip lastTrip = trips.get(trips.size() - 1);

        return mapToTripResponse(lastTrip, vehicle);
    }

    private TripResponse mapToTripResponse(Trip trip, VehicleResponse vehicle) {
        List<TripResponse.PointDto> pointDtos = trip.getPoints().stream()
                .map(point -> TripResponse.PointDto.builder()
                        .ts(point.getTs())
                        .lat(point.getLat())
                        .lon(point.getLon())
                        .speedKph(point.getSpeedKph())
                        .build())
                .collect(Collectors.toList());

        return TripResponse.builder()
                .vehicle(vehicle)
                .startedAt(trip.getStartedAt())
                .endedAt(trip.getEndedAt())
                .distanceKm(trip.getDistanceKm())
                .avgSpeedKph(trip.getAvgSpeedKph())
                .pointCount(trip.getPoints().size())
                .points(pointDtos)
                .build();
    }
}
