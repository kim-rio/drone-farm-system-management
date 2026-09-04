import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  CreateStaffRequest,
  StaffMember,
  StaffRole,
  StaffService,
  UpdateStaffRequest
} from './staff.service';

@Component({
  selector: 'app-staff',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './staff.html',
  styleUrl: './staff.scss'
})
export class Staff {

  private readonly staffService = inject(StaffService);

  staff: StaffMember[] = [];

  loading = signal(true);
  saving = false;

  errorMessage = '';
  successMessage = '';

  showForm = false;
  editingStaff: StaffMember | null = null;

  form = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'MANAGEMENT' as StaffRole
  };

  readonly roles: StaffRole[] = [
    'MANAGEMENT',
    'GEOLOGIST',
    'DRONE_OPERATOR'
  ];

  constructor() {
    this.loadStaff();
  }

  loadStaff(): void {
    this.loading.set(true);
    this.errorMessage = '';

    this.staffService.getStaff().subscribe({
      next: (staff) => {
        console.log('STAFF LOADED:', staff);

        this.staff = staff;
        this.loading.set(false);
      },

      error: (error) => {
        console.error('STAFF LOAD ERROR:', error);

        this.loading.set(false);

        this.errorMessage =
          error?.error?.message ??
          'Unable to load staff.';
      }
    });
  }

  openCreateForm(): void {
    this.editingStaff = null;

    this.form = {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      role: 'MANAGEMENT'
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.saving = false;
    this.showForm = true;
  }

  openEditForm(member: StaffMember): void {
    this.editingStaff = member;

    this.form = {
      firstName: member.firstName,
      lastName: member.lastName,
      email: member.email,
      password: '',
      role: member.role
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.saving = false;
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingStaff = null;
    this.saving = false;
  }

  saveStaff(): void {

    if (this.saving) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    if (
      !this.form.firstName.trim() ||
      !this.form.lastName.trim() ||
      !this.form.email.trim()
    ) {
      this.errorMessage =
        'Please complete all required fields.';
      return;
    }

    if (
      !this.editingStaff &&
      this.form.password.length < 8
    ) {
      this.errorMessage =
        'Password must be at least 8 characters.';
      return;
    }

    this.saving = true;

    if (this.editingStaff) {

      const request: UpdateStaffRequest = {
        firstName: this.form.firstName.trim(),
        lastName: this.form.lastName.trim(),
        email: this.form.email.trim(),
        role: this.form.role
      };

      this.staffService
        .updateStaff(this.editingStaff.id, request)
        .subscribe({

          next: () => {
            this.saving = false;
            this.showForm = false;
            this.editingStaff = null;

            this.successMessage =
              'Staff member updated successfully.';

            this.loadStaff();
          },

          error: (error) => {
            this.saving = false;

            this.errorMessage =
              error?.error?.message ??
              'Unable to update staff member.';
          }
        });

      return;
    }

    const request: CreateStaffRequest = {
      firstName: this.form.firstName.trim(),
      lastName: this.form.lastName.trim(),
      email: this.form.email.trim(),
      password: this.form.password,
      role: this.form.role
    };

    this.staffService
      .createStaff(request)
      .subscribe({

        next: () => {
          this.saving = false;
          this.showForm = false;
          this.editingStaff = null;

          this.successMessage =
            'Staff member created successfully.';

          this.loadStaff();
        },

        error: (error) => {
          this.saving = false;

          this.errorMessage =
            error?.error?.message ??
            'Unable to create staff member.';
        }
      });
  }

  toggleStatus(member: StaffMember): void {

    this.errorMessage = '';
    this.successMessage = '';

    const request$ = member.active
      ? this.staffService.deactivateStaff(member.id)
      : this.staffService.activateStaff(member.id);

    request$.subscribe({

      next: (updatedStaff) => {

        this.staff = this.staff.map(item =>
          item.id === updatedStaff.id
            ? updatedStaff
            : item
        );

        this.successMessage =
          updatedStaff.active
            ? 'Staff member activated.'
            : 'Staff member deactivated.';
      },

      error: (error) => {

        this.errorMessage =
          error?.error?.message ??
          'Unable to change staff status.';
      }
    });
  }

  getRoleLabel(role: StaffRole): string {

    switch (role) {
      case 'DRONE_OPERATOR':
        return 'Drone Operator';

      case 'GEOLOGIST':
        return 'Geologist';

      case 'MANAGEMENT':
        return 'Management';

      default:
        return role;
    }
  }

  getInitials(member: StaffMember): string {

    const first =
      member.firstName?.charAt(0) ?? '';

    const last =
      member.lastName?.charAt(0) ?? '';

    return (first + last).toUpperCase();
  }
}
