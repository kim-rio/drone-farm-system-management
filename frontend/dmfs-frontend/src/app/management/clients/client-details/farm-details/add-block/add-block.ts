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
  Block,
  BlockService
} from '../../../../../services/block.service';

@Component({
  selector: 'app-add-block',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './add-block.html',
  styleUrl: './add-block.scss'
})
export class AddBlock {

  private readonly blockService =
    inject(BlockService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  @Input({ required: true })
  farmId!: number;

  @Output()
  closed = new EventEmitter<void>();

  @Output()
  created = new EventEmitter<Block>();

  loading = false;

  errorMessage = '';

  successMessage = '';

  block = {
    name: '',
    description: '',
    areaHectares: undefined as number | undefined,
    centerLatitude: undefined as number | undefined,
    centerLongitude: undefined as number | undefined
  };

  createBlock(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (!this.block.name.trim()) {
      this.errorMessage =
        'Block name is required.';
      return;
    }

    if (
      this.block.centerLatitude !== undefined &&
      (
        this.block.centerLatitude < -90 ||
        this.block.centerLatitude > 90
      )
    ) {
      this.errorMessage =
        'Latitude must be between -90 and 90.';
      return;
    }

    if (
      this.block.centerLongitude !== undefined &&
      (
        this.block.centerLongitude < -180 ||
        this.block.centerLongitude > 180
      )
    ) {
      this.errorMessage =
        'Longitude must be between -180 and 180.';
      return;
    }

    this.loading = true;

    this.blockService
      .createBlock(
        this.farmId,
        {
          name: this.block.name.trim(),
          description: this.block.description.trim(),
          areaHectares: this.block.areaHectares,
          centerLatitude:
            this.block.centerLatitude,
          centerLongitude:
            this.block.centerLongitude
        }
      )
      .subscribe({

        next: (createdBlock: Block) => {

          console.log(
            'BLOCK CREATED:',
            createdBlock
          );

          this.loading = false;

          this.successMessage =
            'Block added successfully.';

          this.cdr.detectChanges();

          setTimeout(() => {
            this.created.emit(createdBlock);
          }, 500);
        },

        error: (error: unknown) => {

          console.error(
            'BLOCK CREATION ERROR:',
            error
          );

          this.loading = false;

          if (
            typeof error === 'object' &&
            error !== null &&
            'status' in error
          ) {

            const status =
              Number(
                (error as { status: number }).status
              );

            if (status === 400) {
              this.errorMessage =
                'Please check the block information.';
            } else if (status === 401) {
              this.errorMessage =
                'Your session has expired.';
            } else if (status === 403) {
              this.errorMessage =
                'You do not have permission to add blocks.';
            } else {
              this.errorMessage =
                'Unable to add block.';
            }

          } else {

            this.errorMessage =
              'Unable to add block.';
          }

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