package com.dmfs.farm.service;

import com.dmfs.customer.entity.Customer;
import com.dmfs.customer.repository.CustomerRepository;
import com.dmfs.farm.entity.Farm;
import com.dmfs.farm.repository.FarmRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmService {

    private final FarmRepository farmRepository;
    private final CustomerRepository customerRepository;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(
                    new PrecisionModel(),
                    4326
            );

    public FarmService(
            FarmRepository farmRepository,
            CustomerRepository customerRepository
    ) {
        this.farmRepository = farmRepository;
        this.customerRepository = customerRepository;
    }

    public Farm createFarm(
            Long customerId,
            String name,
            String description,
            Double latitude,
            Double longitude,
            Double areaHectares
    ) {

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found"
                                )
                        );

        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "Latitude and longitude are required"
            );
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }

        Farm farm = new Farm();

        farm.setName(name);
        farm.setDescription(description);
        farm.setAreaHectares(areaHectares);
        farm.setCustomer(customer);

        /*
         * IMPORTANT:
         * JTS Coordinate is X = longitude
         * and Y = latitude.
         */
        Point location = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );

        location.setSRID(4326);

        farm.setLocation(location);

        return farmRepository.save(farm);
    }

    public List<Farm> getCustomerFarms(
            Long customerId
    ) {

        return farmRepository.findByCustomerId(customerId);
    }

    public Farm getFarm(Long id) {

        return farmRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found"
                        )
                );
    }

    public void deleteFarm(Long id) {

        if (!farmRepository.existsById(id)) {

            throw new RuntimeException(
                    "Farm not found"
            );
        }

        farmRepository.deleteById(id);
    }
}