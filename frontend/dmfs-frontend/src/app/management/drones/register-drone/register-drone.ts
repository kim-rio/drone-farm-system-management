import {
  Component,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  DroneService
} from '../../../services/drone.service';


@Component({
  selector: 'app-register-drone',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './register-drone.html',
  styleUrl: './register-drone.scss'
})
export class RegisterDrone {

  private readonly fb = inject(FormBuilder);
  private readonly droneService = inject(DroneService);
  private readonly router = inject(Router);


  submitting = false;

  errorMessage = '';


  droneForm = this.fb.nonNullable.group({

    name: [
      '',
      [
        Validators.required,
        Validators.maxLength(100)
      ]
    ],

    serialNumber: [
      '',
      [
        Validators.required,
        Validators.maxLength(100)
      ]
    ],

    model: [
      '',
      [
        Validators.maxLength(100)
      ]
    ],

    manufacturer: [
      '',
      [
        Validators.maxLength(100)
      ]
    ],

    droneType: [
      '',
      [
        Validators.required,
        Validators.maxLength(100)
      ]
    ],

    purchaseDate: [
      ''
    ]

  });


  get name() {
    return this.droneForm.controls.name;
  }


  get serialNumber() {
    return this.droneForm.controls.serialNumber;
  }


  get model() {
    return this.droneForm.controls.model;
  }


  get manufacturer() {
    return this.droneForm.controls.manufacturer;
  }


  get droneType() {
    return this.droneForm.controls.droneType;
  }


  get purchaseDate() {
    return this.droneForm.controls.purchaseDate;
  }


  submit(): void {

    this.errorMessage = '';


    if (this.droneForm.invalid) {

      this.droneForm.markAllAsTouched();

      return;
    }


    this.submitting = true;


    const formValue = this.droneForm.getRawValue();


    this.droneService.createDrone({

      name: formValue.name.trim(),

      serialNumber:
        formValue.serialNumber.trim(),

      model:
        formValue.model.trim() || undefined,

      manufacturer:
        formValue.manufacturer.trim() || undefined,

      droneType:
        formValue.droneType.trim(),

      purchaseDate:
        formValue.purchaseDate || undefined

    }).subscribe({

      next: () => {

        this.submitting = false;

        this.router.navigate([
          '/management/drones'
        ]);

      },


      error: (error: unknown) => {

        console.error(
          'DRONE REGISTRATION ERROR:',
          error
        );


        this.submitting = false;


        if (
          typeof error === 'object' &&
          error !== null &&
          'status' in error
        ) {

          const status =
            (error as {
              status: number
            }).status;


          if (status === 400) {

            this.errorMessage =
              'Please check the drone information and try again.';

          } else if (status === 401) {

            this.errorMessage =
              'Your session has expired. Please log in again.';

          } else if (status === 403) {

            this.errorMessage =
              'You do not have permission to register a drone.';

          } else if (status === 409) {

            this.errorMessage =
              'A drone with this serial number already exists.';

          } else {

            this.errorMessage =
              'Unable to register drone. Please try again.';

          }

        } else {

          this.errorMessage =
            'Unable to register drone. Please try again.';

        }

      }

    });

  }


  cancel(): void {

    this.router.navigate([
      '/management/drones'
    ]);

  }

}