import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  Output,
  inject
} from '@angular/core';

import { FormsModule } from '@angular/forms';

import {
  Farm,
  FarmService
} from '../../../../services/farm.service';

@Component({
  selector: 'app-add-farm',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './add-farm.html',
  styleUrl: './add-farm.scss'
})
export class AddFarm {

  private readonly farmService =
    inject(FarmService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  @Input({ required: true })
  customerId!: number;

  @Output()
  closed = new EventEmitter<void>();

  @Output()
  created = new EventEmitter<Farm>();

  loading = false;

  errorMessage = '';

  successMessage = '';

  farm = {
    name: '',
    description: '',
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,
    areaHectares: undefined as number | undefined
  };

  createFarm(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (!this.farm.name.trim()) {
      this.errorMessage =
        'Farm name is required.';
      return;
    }

    if (
      this.farm.latitude === undefined ||
      this.farm.longitude === undefined
    ) {
      this.errorMessage =
        'Latitude and longitude are required.';
      return;
    }

    if (
      this.farm.latitude < -90 ||
      this.farm.latitude > 90
    ) {
      this.errorMessage =
        'Latitude must be between -90 and 90.';
      return;
    }

    if (
      this.farm.longitude < -180 ||
      this.farm.longitude > 180
    ) {
      this.errorMessage =
        'Longitude must be between -180 and 180.';
      return;
    }

    this.loading = true;

    this.farmService
      .createFarm(
        this.customerId,
        {
          name: this.farm.name.trim(),
          description: this.farm.description.trim(),
          latitude: this.farm.latitude,
          longitude: this.farm.longitude,
          areaHectares: this.farm.areaHectares
        }
      )
      .subscribe({

        next: (farm: Farm) => {

          console.log(
            'FARM CREATED:',
            farm
          );

          this.loading = false;

          this.successMessage =
            'Farm added successfully.';

          this.cdr.detectChanges();

          setTimeout(() => {
            this.created.emit(farm);
          }, 500);
        },

        error: (error: unknown) => {

          console.error(
            'FARM CREATION ERROR:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'Unable to add farm.';

          this.cdr.detectChanges();
        }
      });
  }

  close(): void {

    if (this.loading) {
      return;
    }

    this.closed.emit();
  }

  stopClick(event: MouseEvent): void {
    event.stopPropagation();
  }
}