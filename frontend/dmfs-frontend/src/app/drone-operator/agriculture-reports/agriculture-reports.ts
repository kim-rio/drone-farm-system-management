import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import { OperatorShell } from '../shared/operator-shell/operator-shell';
import { CompanyBrandingService } from '../../services/company-branding.service';
import { HttpErrorResponse } from '@angular/common/http';
import { OperatorService, AgricultureReport } from '../operator.service';

@Component({
  selector:'app-agriculture-reports',
  standalone:true,
  imports:[CommonModule, OperatorShell],
  templateUrl:'./agriculture-reports.html',
  styleUrl:'./agriculture-reports.scss'
})
export class AgricultureReportsPage implements OnInit {
  private readonly api=inject(OperatorService);
  private readonly router=inject(Router);
  private readonly cdr=inject(ChangeDetectorRef);
  private readonly brandingService=inject(CompanyBrandingService);
  reports:AgricultureReport[]=[];
  loading=true;
  error='';
  selected:AgricultureReport|null=null;
  companyName='Company';

  ngOnInit(){
    this.load();
    this.brandingService.getBranding().subscribe({ next: b => this.companyName = b.companyName || 'Company' });
  }
  load(){
    this.loading=true;
    this.api.getAgricultureReports().subscribe({
      next:r=>{this.reports=r||[];this.loading=false;this.cdr.detectChanges();},
      error:(e: HttpErrorResponse)=>{this.error=e?.error?.message||'Unable to load agricultural reports.';this.loading=false;this.cdr.detectChanges();}
    });
  }
  get finalizedCount(){ return this.reports.filter(r => r.finalized).length; }
  open(r:AgricultureReport){this.selected=r;}
  close(){this.selected=null;}
  edit(r:AgricultureReport){this.router.navigate(['/drone-operator/agriculture-reports/mission',r.missionId]);}
  back(){this.router.navigate(['/drone-operator']);}
  download(r: AgricultureReport): void {
    this.selected = r;
    document.body.classList.add('printing-report');
    setTimeout(() => window.print(), 50);
    window.addEventListener('afterprint', () => document.body.classList.remove('printing-report'), { once: true });
  }
}
