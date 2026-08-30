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
  Farm,
  FarmService
} from '../../../../services/farm.service';

import {
  Block,
  BlockService
} from '../../../../services/block.service';

import { AddBlock } from './add-block/add-block';

@Component({
  selector: 'app-farm-details',
  standalone: true,
  imports: [AddBlock],
  templateUrl: './farm-details.html',
  styleUrl: './farm-details.scss'
})
export class FarmDetails implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly farmService =
    inject(FarmService);

  private readonly blockService =
    inject(BlockService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  clientId = 0;

  farmId = 0;

  farm: Farm | null = null;

  blocks: Block[] = [];

  loading = true;

  blocksLoading = true;

  errorMessage = '';

  blocksErrorMessage = '';

  showAddBlock = false;

  ngOnInit(): void {

    const clientIdParam =
      this.route.snapshot.paramMap.get(
        'clientId'
      );

    const farmIdParam =
      this.route.snapshot.paramMap.get(
        'farmId'
      );

    this.clientId =
      Number(clientIdParam);

    this.farmId =
      Number(farmIdParam);

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

      this.cdr.detectChanges();

      return;
    }

    this.loadFarm();

    this.loadBlocks();
  }

  // ==========================================
  // LOAD FARM
  // ==========================================

  loadFarm(): void {

    this.loading = true;

    this.farmService
      .getFarm(this.farmId)
      .subscribe({

        next: (farm: Farm) => {

          console.log(
            'FARM DETAILS:',
            farm
          );

          this.farm = farm;

          this.loading = false;

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

  // ==========================================
  // LOAD BLOCKS
  // ==========================================

  loadBlocks(): void {

    this.blocksLoading = true;

    this.blocksErrorMessage = '';

    this.blockService
      .getFarmBlocks(this.farmId)
      .subscribe({

        next: (blocks: Block[]) => {

          console.log(
            'FARM BLOCKS:',
            blocks
          );

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

  // ==========================================
  // ADD BLOCK
  // ==========================================

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

    this.cdr.detectChanges();
  }

  // ==========================================
  // BACK TO CLIENT
  // ==========================================

  backToClient(): void {

    this.router.navigate([
      '/management/clients',
      this.clientId
    ]);
  }
}