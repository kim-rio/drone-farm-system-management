import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Mission, OperatorService, Survey } from './operator.service';

@Component({ selector: 'app-operator-workspace', standalone: true, imports: [CommonModule, RouterLink, DatePipe], templateUrl: './operator-workspace.html', styleUrl: './operator-workspace.scss' })
export class OperatorWorkspace implements OnInit {
  private api = inject(OperatorService); private auth = inject(AuthService); private route = inject(ActivatedRoute); private router = inject(Router);
  page = 'dashboard'; missions: Mission[] = []; surveys: Survey[] = []; loading = true; busy = false; error = ''; selected?: Mission;
  user = this.auth.getCurrentUser();
  ngOnInit() { this.page = this.route.snapshot.data['page'] || 'dashboard'; const id = Number(this.route.snapshot.paramMap.get('id')); this.load(id || undefined); }
  load(id?: number) { this.loading = true; forkJoin({ missions: this.api.missions(), surveys: this.api.surveys() }).subscribe({ next: d => { this.missions=d.missions; this.surveys=d.surveys; this.selected=id ? this.missions.find(m=>m.id===id) : undefined; this.loading=false; }, error: () => { this.error='We could not load your field workspace. Please refresh and try again.'; this.loading=false; } }); }
  get name() { return this.user?.firstName || 'Operator'; } get initials() { return `${this.user?.firstName?.[0]||'O'}${this.user?.lastName?.[0]||''}`; }
  get pending() { return this.missions.filter(m=>m.status==='ASSIGNED'); } get today() { const date = new Date().toISOString().slice(0,10); return this.missions.filter(m=>m.scheduledDate===date); } get active() { return this.missions.filter(m=>m.status==='IN_PROGRESS'); } get completed() { return this.missions.filter(m=>m.status==='COMPLETED'); }
  statusLabel(s: string) { return s.replace('_', ' '); } surveyFor(m: Mission) { return this.surveys.find(s=>s.serviceRequestId === m.serviceRequest?.id); }
  action(action: 'accept'|'start'|'complete', mission: Mission) { this.busy=true; const call=action==='accept'?this.api.accept(mission.id):action==='start'?this.api.startMission(mission.id):this.api.completeMission(mission.id); call.subscribe({next:()=>this.load(mission.id), error: e=>{this.error=e?.error?.message||'The mission could not be updated. Please try again.';this.busy=false;}}); }
  launchSurvey(mission: Mission) { const survey=this.surveyFor(mission); if(survey) { this.router.navigate(['/drone-operator/surveys']); return; } this.busy=true; const now=new Date().toISOString(); this.api.createSurvey({ serviceRequestId: mission.serviceRequest?.id, operatorId:this.user?.userId, surveyCode:`SRV-${mission.missionCode.replace('MIS-','')}`, surveyName:`${mission.missionCode} field survey`, startedAt:now, startLatitude:mission.farmBlock?.centerLatitude, startLongitude:mission.farmBlock?.centerLongitude }).subscribe({next:()=>this.load(mission.id),error:e=>{this.error=e?.error?.message||'Unable to create the survey.';this.busy=false;}}); }
  surveyAction(action: 'start'|'complete', survey: Survey) { this.busy=true; (action==='start'?this.api.startSurvey(survey.id):this.api.completeSurvey(survey.id)).subscribe({next:()=>this.load(),error:()=>{this.error='The survey could not be updated.';this.busy=false;}}); }
  logout() { this.auth.logout().subscribe({complete:()=>this.router.navigate(['/login']),error:()=>this.router.navigate(['/login'])}); }
}
