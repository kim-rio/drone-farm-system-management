import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  FormsModule
} from '@angular/forms';

import {
  Farm,
  FarmService,
  UpdateFarmRequest
} from '../../../../services/farm.service';

import {
  Block,
  BlockService,
  UpdateBlockRequest
} from '../../../../services/block.service';

import { AddBlock } from './add-block/add-block';

@Component({
  selector: 'app-farm-details',
  standalone: true,
  imports: [
    FormsModule,
    AddBlock
  ],
  templateUrl: './farm-details.html',
  styleUrl: './farm-details.scss'
})
export class FarmDetails implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly farmService = inject(FarmService);
  private readonly blockService = inject(BlockService);
  private readonly cdr = inject(ChangeDetectorRef);

  clientId = 0;
  farmId = 0;

  farm: Farm | null = null;
  blocks: Block[] = [];

  loading = true;
  blocksLoading = true;
  saving = false;
  blockSaving = false;

  errorMessage = '';
  blocksErrorMessage = '';
  saveErrorMessage = '';
  blockErrorMessage = '';
  successMessage = '';

  showAddBlock = false;
  editMode = false;
  editingBlockId: number | null = null;

  blockAuditMessage = '';

  editFarm: UpdateFarmRequest = {
    name: '',
    description: '',
    latitude: 0,
    longitude: 0,
    areaHectares: undefined
  };

  editBlock: UpdateBlockRequest = {
    name: '',
    description: '',
    areaHectares: undefined,
    centerLatitude: undefined,
    centerLongitude: undefined
  };

  ngOnInit(): void {
    this.clientId =
      Number(
        this.route.snapshot.paramMap.get('clientId')
      );

    this.farmId =
      Number(
        this.route.snapshot.paramMap.get('farmId')
      );

    if (
      !this.clientId ||
      !this.farmId ||
      Number.isNaN(this.clientId) ||
      Number.isNaN(this.farmId)
    ) {
      this.loading = false;
      this.blocksLoading = false;
      this.errorMessage =
        'Invalid client or farm ID.';
      return;
    }

    this.loadFarm();
    this.loadBlocks();
  }

  loadFarm(): void {
    this.loading = true;

    this.farmService
      .getFarm(this.farmId)
      .subscribe({
        next: (farm: Farm) => {
          this.farm = farm;
          this.loading = false;

          if (
            this.route.snapshot.queryParamMap.get('edit') ===
            'true'
          ) {
            this.startEdit();
          }

          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          console.error(
            'FARM DETAILS ERROR:',
            error
          );

          this.loading = false;
          this.errorMessage =
            'Unable to load farm details.';

          this.cdr.detectChanges();
        }
      });
  }

  loadBlocks(): void {
    this.blocksLoading = true;
    this.blocksErrorMessage = '';

    this.blockService
      .getFarmBlocks(this.farmId)
      .subscribe({
        next: (blocks: Block[]) => {
          this.blocks = blocks;
          this.blocksLoading = false;
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          console.error(
            'BLOCKS ERROR:',
            error
          );

          this.blocksLoading = false;
          this.blocksErrorMessage =
            'Unable to load blocks.';

          this.cdr.detectChanges();
        }
      });
  }

  startEdit(): void {
    if (!this.farm || this.saving) {
      return;
    }

    this.saveErrorMessage = '';
    this.successMessage = '';

    this.editFarm = {
      name: this.farm.name ?? '',
      description: this.farm.description ?? '',
      latitude: this.farm.latitude ?? 0,
      longitude: this.farm.longitude ?? 0,
      areaHectares:
        this.farm.areaHectares ?? undefined
    };

    this.editMode = true;
    this.cdr.detectChanges();
  }

  cancelEdit(): void {
    if (this.saving) {
      return;
    }

    this.editMode = false;
    this.saveErrorMessage = '';
    this.cdr.detectChanges();
  }

  saveFarm(): void {
    this.saveErrorMessage = '';
    this.successMessage = '';

    const validationError =
      this.validateEditFarm();

    if (validationError) {
      this.saveErrorMessage = validationError;
      return;
    }

    this.saving = true;

    this.farmService
      .updateFarm(
        this.farmId,
        {
          name: this.editFarm.name.trim(),
          description:
            this.editFarm.description?.trim(),
          latitude:
            Number(this.editFarm.latitude),
          longitude:
            Number(this.editFarm.longitude),
          areaHectares:
            this.editFarm.areaHectares ===
              undefined ||
            this.editFarm.areaHectares === null
              ? undefined
              : Number(this.editFarm.areaHectares)
        }
      )
      .subscribe({
        next: (updatedFarm: Farm) => {
          this.farm = updatedFarm;
          this.saving = false;
          this.editMode = false;
          this.successMessage =
            'Farm updated successfully.';
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          console.error(
            'FARM UPDATE ERROR:',
            error
          );

          this.saving = false;
          this.saveErrorMessage =
            this.extractErrorMessage(
              error,
              'Unable to update farm.'
            );

          this.cdr.detectChanges();
        }
      });
  }

  private validateEditFarm(): string | null {
    if (!this.editFarm.name?.trim()) {
      return 'Farm name is required.';
    }

    if (
      this.editFarm.latitude === undefined ||
      this.editFarm.latitude === null ||
      Number.isNaN(
        Number(this.editFarm.latitude)
      )
    ) {
      return 'Latitude is required.';
    }

    if (
      Number(this.editFarm.latitude) < -90 ||
      Number(this.editFarm.latitude) > 90
    ) {
      return 'Latitude must be between -90 and 90.';
    }

    if (
      this.editFarm.longitude === undefined ||
      this.editFarm.longitude === null ||
      Number.isNaN(
        Number(this.editFarm.longitude)
      )
    ) {
      return 'Longitude is required.';
    }

    if (
      Number(this.editFarm.longitude) < -180 ||
      Number(this.editFarm.longitude) > 180
    ) {
      return 'Longitude must be between -180 and 180.';
    }

    if (
      this.editFarm.areaHectares !== undefined &&
      this.editFarm.areaHectares !== null &&
      Number(this.editFarm.areaHectares) <= 0
    ) {
      return 'Farm area must be greater than zero.';
    }

    return null;
  }

  startBlockEdit(block: Block): void {
    if (this.blockSaving) {
      return;
    }

    this.blockErrorMessage = '';
    this.successMessage = '';

    this.editingBlockId = block.id;

    this.editBlock = {
      name: block.name ?? '',
      description: block.description ?? '',
      areaHectares:
        block.areaHectares ?? undefined,
      centerLatitude:
        block.centerLatitude ?? undefined,
      centerLongitude:
        block.centerLongitude ?? undefined
    };

    this.cdr.detectChanges();
  }

  cancelBlockEdit(): void {
    if (this.blockSaving) {
      return;
    }

    this.editingBlockId = null;
    this.blockErrorMessage = '';
    this.cdr.detectChanges();
  }

  saveBlock(block: Block): void {
    this.blockErrorMessage = '';
    this.successMessage = '';

    const validationError =
      this.validateBlock();

    if (validationError) {
      this.blockErrorMessage = validationError;
      return;
    }

    this.blockSaving = true;

    this.blockService
      .updateBlock(
        block.id,
        {
          name: this.editBlock.name.trim(),
          description:
            this.editBlock.description?.trim(),
          areaHectares:
            Number(this.editBlock.areaHectares),
          centerLatitude:
            this.editBlock.centerLatitude ===
              undefined
              ? undefined
              : Number(
                  this.editBlock.centerLatitude
                ),
          centerLongitude:
            this.editBlock.centerLongitude ===
              undefined
              ? undefined
              : Number(
                  this.editBlock.centerLongitude
                )
        }
      )
      .subscribe({
        next: (updatedBlock: Block) => {
          this.blocks = this.blocks.map(
            current =>
              current.id === updatedBlock.id
                ? updatedBlock
                : current
          );

          this.blockSaving = false;
          this.editingBlockId = null;
          this.successMessage =
            'Block updated successfully.';

          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          console.error(
            'BLOCK UPDATE ERROR:',
            error
          );

          this.blockSaving = false;
          this.blockErrorMessage =
            this.extractErrorMessage(
              error,
              'Unable to update block.'
            );

          this.cdr.detectChanges();
        }
      });
  }

  deleteBlock(block: Block): void {
    if (this.blockSaving) {
      return;
    }

    const confirmed =
      window.confirm(
        `Delete "${block.name}"? This action cannot be undone.`
      );

    if (!confirmed) {
      return;
    }

    this.blockSaving = true;
    this.blockErrorMessage = '';
    this.successMessage = '';

    this.blockService
      .deleteBlock(block.id)
      .subscribe({
        next: () => {
          this.blocks = this.blocks.filter(
            current =>
              current.id !== block.id
          );

          this.blockSaving = false;
          this.successMessage =
            'Block deleted successfully.';

          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          console.error(
            'BLOCK DELETE ERROR:',
            error
          );

          this.blockSaving = false;
          this.blockErrorMessage =
            this.extractErrorMessage(
              error,
              'Unable to delete block.'
            );

          this.cdr.detectChanges();
        }
      });
  }

  private validateBlock(): string | null {
    if (!this.editBlock.name?.trim()) {
      return 'Block name is required.';
    }

    if (
      this.editBlock.areaHectares === undefined ||
      this.editBlock.areaHectares === null ||
      Number(this.editBlock.areaHectares) <= 0
    ) {
      return 'Block area must be greater than zero.';
    }

    if (
      this.editBlock.centerLatitude !== undefined &&
      this.editBlock.centerLatitude !== null &&
      (
        Number(this.editBlock.centerLatitude) < -90 ||
        Number(this.editBlock.centerLatitude) > 90
      )
    ) {
      return 'Latitude must be between -90 and 90.';
    }

    if (
      this.editBlock.centerLongitude !== undefined &&
      this.editBlock.centerLongitude !== null &&
      (
        Number(this.editBlock.centerLongitude) < -180 ||
        Number(this.editBlock.centerLongitude) > 180
      )
    ) {
      return 'Longitude must be between -180 and 180.';
    }

    return null;
  }

  addBlock(): void {
    this.showAddBlock = true;
    this.cdr.detectChanges();
  }

  closeAddBlock(): void {
    this.showAddBlock = false;
    this.cdr.detectChanges();
  }

  blockCreated(): void {
    this.showAddBlock = false;
    this.loadBlocks();
    this.successMessage =
      'Block added successfully.';
    this.cdr.detectChanges();
  }

  auditBlocks(): void {
    if (!this.farm) {
      return;
    }

    const allocated =
      this.blocks.reduce(
        (sum, block) =>
          sum + (block.areaHectares ?? 0),
        0
      );

    const missingArea =
      this.blocks.filter(
        block =>
          !block.areaHectares ||
          block.areaHectares <= 0
      ).length;

    if (missingArea) {
      this.blockAuditMessage =
        `${missingArea} block(s) need an area before they can be used for a service request.`;
    } else if (
      this.farm.areaHectares &&
      allocated > this.farm.areaHectares
    ) {
      this.blockAuditMessage =
        `Block area (${allocated} ha) exceeds the farm area (${this.farm.areaHectares} ha). Fix block areas.`;
    } else {
      const remaining =
        this.farm.areaHectares
          ? this.farm.areaHectares - allocated
          : 0;

      this.blockAuditMessage =
        `Check passed: ${this.blocks.length} block(s) are valid${
          this.farm.areaHectares
            ? `; ${remaining.toFixed(2)} ha remains unallocated.`
            : '.'
        }`;
    }

    this.cdr.detectChanges();
  }

  private extractErrorMessage(
    error: unknown,
    fallback: string
  ): string {
    if (
      typeof error === 'object' &&
      error !== null &&
      'error' in error
    ) {
      const response =
        error as {
          error?: {
            message?: string;
          };
        };

      return (
        response.error?.message ??
        fallback
      );
    }

    return fallback;
  }

  backToClient(): void {
    this.router.navigate([
      '/management/clients',
      this.clientId
    ]);
  }
}
