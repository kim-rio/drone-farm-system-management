import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  ServiceRequest,
  ServiceRequestService
} from '../../../services/service-request.service';

@Component({
  selector: 'app-service-request-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './service-request-list.html',
  styleUrl: './service-request-list.scss'
})
export class ServiceRequestList implements OnInit {

  private readonly requestService =
    inject(ServiceRequestService);

  private readonly router =
    inject(Router);

  private readonly cdr =
    inject(ChangeDetectorRef);


  requests: ServiceRequest[] = [];

  loading = true;

  errorMessage = '';

  successMessage = '';

  searchTerm = '';

  selectedStatus = 'ALL';


  ngOnInit(): void {
    this.loadRequests();
  }


  /* ==============================
     LOAD REQUESTS
     ============================== */

  loadRequests(): void {

    this.loading = true;

    this.errorMessage = '';

    this.requestService
      .getRequests()
      .subscribe({

        next: (requests) => {

          console.log(
            'SERVICE REQUESTS RECEIVED:',
            requests
          );

          this.requests = requests;

          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'SERVICE REQUEST LOAD ERROR:',
            error
          );

          this.loading = false;

          if (error?.status === 401) {

            this.errorMessage =
              'Your session has expired. Please log in again.';

          } else if (error?.status === 403) {

            this.errorMessage =
              'You do not have permission to view service requests.';

          } else {

            this.errorMessage =
              'Unable to load service requests.';
          }

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     FILTER
     ============================== */

  get filteredRequests(): ServiceRequest[] {

    const search =
      this.searchTerm
        .trim()
        .toLowerCase();

    return this.requests.filter(request => {

      const customerName =
        this.getCustomerName(request)
          .toLowerCase();

      const farmName =
        request.farm?.name
          ?.toLowerCase() ?? '';

      const blockName =
        request.farmBlock?.name
          ?.toLowerCase() ?? '';

      const serviceName =
        request.serviceCatalogue?.name
          ?.toLowerCase() ?? '';

      const matchesSearch =
        !search ||
        customerName.includes(search) ||
        farmName.includes(search) ||
        blockName.includes(search) ||
        serviceName.includes(search) ||
        String(request.id).includes(search);

      const matchesStatus =
        this.selectedStatus === 'ALL' ||
        request.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }


  /* ==============================
     CUSTOMER NAME
     ============================== */

  getCustomerName(
    request: ServiceRequest
  ): string {

    const customer =
      request.customer;

    if (!customer) {
      return 'Unknown Customer';
    }

    if (customer.companyName?.trim()) {
      return customer.companyName;
    }

    const fullName =
      `${customer.firstName ?? ''} ${customer.lastName ?? ''}`
        .trim();

    return fullName ||
      customer.customerCode ||
      'Unknown Customer';
  }


  /* ==============================
     STATUS
     ============================== */

  getStatusClass(
    status: string
  ): string {

    return status
      .toLowerCase()
      .replace(/\s+/g, '-');
  }


  /* ==============================
     CREATE
     ============================== */

  createRequest(): void {

    this.router.navigate([
      '/management/service-requests/new'
    ]);
  }


  /* ==============================
     VIEW
     ============================== */

  viewRequest(
    request: ServiceRequest
  ): void {

    this.router.navigate([
      '/management/service-requests',
      request.id
    ]);
  }


  /* ==============================
     STATUS UPDATE
     ============================== */

  updateStatus(
    request: ServiceRequest,
    status: string
  ): void {

    this.errorMessage = '';

    this.requestService
      .updateStatus(
        request.id,
        status
      )
      .subscribe({

        next: () => {

          request.status = status;

          this.successMessage =
            `Request #${request.id} updated to ${status}.`;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'STATUS UPDATE ERROR:',
            error
          );

          this.errorMessage =
            'Unable to update request status.';

          this.cdr.detectChanges();
        }
      });
  }


  /* ==============================
     DELETE
     ============================== */

  deleteRequest(
    request: ServiceRequest
  ): void {

    const confirmed =
      window.confirm(
        `Delete service request #${request.id}?`
      );

    if (!confirmed) {
      return;
    }

    this.errorMessage = '';

    this.requestService
      .deleteRequest(request.id)
      .subscribe({

        next: () => {

          this.requests =
            this.requests.filter(
              item =>
                item.id !== request.id
            );

          this.successMessage =
            `Service request #${request.id} deleted successfully.`;

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'DELETE REQUEST ERROR:',
            error
          );

          this.errorMessage =
            'Unable to delete service request.';

          this.cdr.detectChanges();
        }
      });
  }
}