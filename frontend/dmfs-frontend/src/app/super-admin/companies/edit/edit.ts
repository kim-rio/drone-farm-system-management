import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CompanyResponse,
  SuperAdminService,
  UpdateCompanyRequest
} from '../../../services/super-admin.service';

@Component({
  selector: 'app-edit-company',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './edit.html',
  styleUrl: './edit.scss'
})
export class EditCompany implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(SuperAdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  company: CompanyResponse | null = null;

  loading = true;
  submitting = false;
  error = '';

  readonly companyForm = this.fb.group({
    name: ['', [
      Validators.required,
      Validators.maxLength(150)
    ]],

    registrationNumber: ['', [
      Validators.required,
      Validators.maxLength(100)
    ]],

    tin: ['', [
      Validators.maxLength(100)
    ]],

    email: ['', [
      Validators.required,
      Validators.email,
      Validators.maxLength(100)
    ]],

    phone: ['', [
      Validators.required,
      Validators.maxLength(30)
    ]],

    country: ['', [
      Validators.required,
      Validators.maxLength(100)
    ]],

    region: ['', [
      Validators.required,
      Validators.maxLength(100)
    ]],

    city: ['', [
      Validators.required,
      Validators.maxLength(100)
    ]],

    physicalAddress: ['', [
      Validators.required,
      Validators.maxLength(255)
    ]]
  });

  ngOnInit(): void {

    const idParam =
      this.route.snapshot.paramMap.get('id');

    console.log(
      'Edit Company route ID:',
      idParam
    );

    const id = Number(idParam);

    if (!id || Number.isNaN(id)) {

      this.error = 'Invalid company ID.';
      this.loading = false;

      this.cdr.detectChanges();

      return;
    }

    this.loadCompany(id);
  }

  private loadCompany(id: number): void {

    this.loading = true;
    this.error = '';

    console.log(
      'Loading company:',
      id
    );

    this.service.getCompany(id).subscribe({

      next: (company) => {

        console.log(
          'Company received:',
          company
        );

        this.company = company;

        this.companyForm.patchValue({
          name: company.name ?? '',
          registrationNumber:
            company.registrationNumber ?? '',
          tin: company.tin ?? '',
          email: company.email ?? '',
          phone: company.phone ?? '',
          country: company.country ?? '',
          region: company.region ?? '',
          city: company.city ?? '',
          physicalAddress:
            company.physicalAddress ?? ''
        });

        this.loading = false;

        console.log(
          'Loading state:',
          this.loading
        );

        console.log(
          'Company state:',
          this.company
        );

        /*
         * Force Angular to immediately update
         * the template after the HTTP response.
         */
        this.cdr.detectChanges();

        console.log(
          'Edit form loaded.'
        );
      },

      error: (err) => {

        console.error(
          'Failed to load company:',
          err
        );

        this.error =
          err?.error?.message ||
          `Failed to load company ${id}.`;

        this.loading = false;

        this.cdr.detectChanges();
      },

      complete: () => {

        console.log(
          'Company request completed.'
        );
      }

    });
  }

  fieldInvalid(
    control: AbstractControl | null
  ): boolean {

    return !!control &&
      control.invalid &&
      (control.touched || control.dirty);
  }

  back(): void {

    if (this.company) {

      this.router.navigate([
        '/super-admin/companies',
        this.company.id
      ]);

    } else {

      this.router.navigate([
        '/super-admin/companies'
      ]);
    }
  }

  goToDashboard(): void {

    this.router.navigate([
      '/super-admin'
    ]);
  }

  goToCompanies(): void {

    this.router.navigate([
      '/super-admin/companies'
    ]);
  }

  logout(): void {

    this.router.navigate([
      '/login'
    ]);
  }

  submit(): void {

    this.error = '';

    if (this.companyForm.invalid) {

      this.companyForm.markAllAsTouched();

      this.cdr.detectChanges();

      return;
    }

    if (!this.company) {

      this.error =
        'Company information is unavailable.';

      this.cdr.detectChanges();

      return;
    }

    this.submitting = true;

    const request: UpdateCompanyRequest = {

      name:
        this.companyForm.controls.name.value!
          .trim(),

      registrationNumber:
        this.companyForm.controls
          .registrationNumber.value!
          .trim(),

      tin:
        this.companyForm.controls.tin.value
          ?.trim() || undefined,

      email:
        this.companyForm.controls.email.value!
          .trim(),

      phone:
        this.companyForm.controls.phone.value!
          .trim(),

      country:
        this.companyForm.controls.country.value!
          .trim(),

      region:
        this.companyForm.controls.region.value!
          .trim(),

      city:
        this.companyForm.controls.city.value!
          .trim(),

      physicalAddress:
        this.companyForm.controls
          .physicalAddress.value!
          .trim()
    };

    console.log(
      'Updating company:',
      this.company.id
    );

    console.log(
      'Update request:',
      request
    );

    this.service.updateCompany(
      this.company.id,
      request
    ).subscribe({

      next: (updated) => {

        console.log(
          'Company updated:',
          updated
        );

        this.submitting = false;

        this.cdr.detectChanges();

        this.router.navigate([
          '/super-admin/companies',
          updated.id
        ]);
      },

      error: (err) => {

        console.error(
          'Failed to update company:',
          err
        );

        this.submitting = false;

        this.error =
          err?.error?.message ||
          'Failed to update company. Please try again.';

        this.cdr.detectChanges();
      }

    });
  }
}
