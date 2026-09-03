import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import {
  CreateCompanyRequest,
  SuperAdminService
} from '../../../services/super-admin.service';

function passwordsMatch(
  control: AbstractControl
): ValidationErrors | null {

  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  if (!password || !confirmPassword) {
    return null;
  }

  return password === confirmPassword
    ? null
    : { passwordsMismatch: true };
}

@Component({
  selector: 'app-register-company',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterCompany {

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly service = inject(SuperAdminService);

  submitting = false;
  error = '';

  readonly companyForm = this.fb.group(
    {
      company: this.fb.group({
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
        ]]
      }),

      location: this.fb.group({
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
      }),

      admin: this.fb.group(
        {
          firstName: ['', [
            Validators.required,
            Validators.maxLength(100)
          ]],

          lastName: ['', [
            Validators.required,
            Validators.maxLength(100)
          ]],

          email: ['', [
            Validators.required,
            Validators.email,
            Validators.maxLength(100)
          ]],

          password: ['', [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(100)
          ]],

          confirmPassword: ['', [
            Validators.required
          ]]
        },
        {
          validators: passwordsMatch
        }
      )
    }
  );

  get company() {
    return this.companyForm.controls.company;
  }

  get location() {
    return this.companyForm.controls.location;
  }

  get admin() {
    return this.companyForm.controls.admin;
  }

  fieldInvalid(
    control: AbstractControl | null
  ): boolean {
    return !!control &&
      control.invalid &&
      (control.touched || control.dirty);
  }

  passwordsDoNotMatch(): boolean {
    return this.admin.hasError('passwordsMismatch') &&
      this.admin.controls.confirmPassword.touched;
  }

  goToDashboard(): void {
    this.router.navigate(['/super-admin']);
  }

  logout(): void {
    this.router.navigate(['/login']);
  }

  cancel(): void {
    this.router.navigate(['/super-admin/companies']);
  }

  submit(): void {

    this.error = '';

    if (this.companyForm.invalid) {
      this.companyForm.markAllAsTouched();
      return;
    }

    this.submitting = true;

    const request: CreateCompanyRequest = {
      name: this.company.controls.name.value!.trim(),
      registrationNumber:
        this.company.controls.registrationNumber.value!.trim(),
      tin:
        this.company.controls.tin.value?.trim() || undefined,
      email: this.company.controls.email.value!.trim(),
      phone: this.company.controls.phone.value!.trim(),

      country:
        this.location.controls.country.value!.trim(),
      region:
        this.location.controls.region.value!.trim(),
      city:
        this.location.controls.city.value!.trim(),
      physicalAddress:
        this.location.controls.physicalAddress.value!.trim(),

      initialAdmin: {
        firstName:
          this.admin.controls.firstName.value!.trim(),
        lastName:
          this.admin.controls.lastName.value!.trim(),
        email:
          this.admin.controls.email.value!.trim(),
        password:
          this.admin.controls.password.value!
      }
    };

    this.service.createCompany(request).subscribe({

      next: () => {
        this.submitting = false;
        this.router.navigate(['/super-admin/companies']);
      },

      error: (err) => {
        console.error(
          'Failed to register company:',
          err
        );

        this.submitting = false;

        this.error =
          err?.error?.message ||
          'Failed to register company. Please try again.';
      }

    });
  }
}

