package com.dmfs.service.service;

import com.dmfs.customer.entity.Customer;
import com.dmfs.farm.entity.Block;
import com.dmfs.farm.entity.Farm;
import com.dmfs.service.dto.ServiceRequestResponse;
import com.dmfs.service.entity.ServiceCatalogue;
import com.dmfs.service.entity.ServiceRequest;

public class ServiceRequestMapper {

    private ServiceRequestMapper() {
    }


    public static ServiceRequestResponse toResponse(
            ServiceRequest request
    ) {

        ServiceRequestResponse response =
                new ServiceRequestResponse();

        response.setId(request.getId());

        response.setRequestedDate(
                request.getRequestedDate()
        );

        response.setNotes(
                request.getNotes()
        );

        response.setStatus(
                request.getStatus()
        );

        response.setCreatedAt(
                request.getCreatedAt()
        );

        response.setUpdatedAt(
                request.getUpdatedAt()
        );


        /* ==============================
           CUSTOMER
           ============================== */

        Customer customer =
                request.getCustomer();

        if (customer != null) {

            ServiceRequestResponse.CustomerInfo
                    customerInfo =
                    new ServiceRequestResponse.CustomerInfo();

            customerInfo.setId(
                    customer.getId()
            );

            customerInfo.setCustomerCode(
                    customer.getCustomerCode()
            );

            customerInfo.setType(
                    customer.getType() != null
                            ? customer.getType().name()
                            : null
            );

            customerInfo.setCompanyName(
                    customer.getCompanyName()
            );

            customerInfo.setFirstName(
                    customer.getFirstName()
            );

            customerInfo.setLastName(
                    customer.getLastName()
            );

            customerInfo.setEmail(
                    customer.getEmail()
            );

            customerInfo.setPhone(
                    customer.getPhone()
            );

            customerInfo.setStatus(
                    customer.getStatus() != null
                            ? customer.getStatus().name()
                            : null
            );

            response.setCustomer(
                    customerInfo
            );
        }


        /* ==============================
           FARM
           ============================== */

        Farm farm =
                request.getFarm();

        if (farm != null) {

            ServiceRequestResponse.FarmInfo
                    farmInfo =
                    new ServiceRequestResponse.FarmInfo();

            farmInfo.setId(
                    farm.getId()
            );

            farmInfo.setName(
                    farm.getName()
            );

            farmInfo.setDescription(
                    farm.getDescription()
            );

            farmInfo.setAreaHectares(
                    farm.getAreaHectares()
            );

            // IMPORTANT:
            // farm.location is intentionally NOT mapped.

            response.setFarm(
                    farmInfo
            );
        }


        /* ==============================
           BLOCK
           ============================== */

        Block block =
                request.getFarmBlock();

        if (block != null) {

            ServiceRequestResponse.BlockInfo
                    blockInfo =
                    new ServiceRequestResponse.BlockInfo();

            blockInfo.setId(
                    block.getId()
            );

            blockInfo.setName(
                    block.getName()
            );

            blockInfo.setDescription(
                    block.getDescription()
            );

            blockInfo.setAreaHectares(
                    block.getAreaHectares()
            );

            blockInfo.setCenterLatitude(
                    block.getCenterLatitude()
            );

            blockInfo.setCenterLongitude(
                    block.getCenterLongitude()
            );

            response.setFarmBlock(
                    blockInfo
            );
        }


        /* ==============================
           SERVICE CATALOGUE
           ============================== */

        ServiceCatalogue catalogue =
                request.getServiceCatalogue();

        if (catalogue != null) {

            ServiceRequestResponse
                    .ServiceCatalogueInfo
                    catalogueInfo =
                    new ServiceRequestResponse
                            .ServiceCatalogueInfo();

            catalogueInfo.setId(
                    catalogue.getId()
            );

            catalogueInfo.setName(
                    catalogue.getName()
            );

            catalogueInfo.setCategory(
                    catalogue.getCategory()
            );

            catalogueInfo.setDescription(
                    catalogue.getDescription()
            );

            catalogueInfo.setStatus(
                    catalogue.getStatus()
            );

            catalogueInfo.setUnitOfMeasurement(
                    catalogue.getUnitOfMeasurement()
            );

            catalogueInfo.setStandardPrice(
                    catalogue.getStandardPrice()
            );

            catalogueInfo.setMinimumArea(
                    catalogue.getMinimumArea()
            );

            catalogueInfo.setEstimatedDurationMinutes(
                    catalogue.getEstimatedDurationMinutes()
            );

            response.setServiceCatalogue(
                    catalogueInfo
            );
        }


        return response;
    }
}