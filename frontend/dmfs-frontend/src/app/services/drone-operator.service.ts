import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
export interface Operation { id:number; clientName:string; farmName:string; blockName:string; serviceName:string; requestedDate:string; notes?:string; status:string; operatorDecisionReason?:string; }
export interface OperatorSurvey { id:number; surveyCode:string; surveyName:string; clientName:string; farmName:string; blockName:string; status:string; startedAt?:string; completedAt?:string; equipmentUsed?:string; }
export interface CompleteSurvey { equipmentUsed:string; minLatitude:number; minLongitude:number; maxLatitude:number; maxLongitude:number; }
@Injectable({providedIn:'root'}) export class DroneOperatorService {
 private readonly http=inject(HttpClient); private readonly api='http://localhost:8080/api/drone-operator';
 operations():Observable<Operation[]>{return this.http.get<Operation[]>(`${this.api}/operations`,{withCredentials:true});}
 accept(id:number):Observable<OperatorSurvey>{return this.http.post<OperatorSurvey>(`${this.api}/operations/${id}/accept`,{},{withCredentials:true});}
 reject(id:number,reason:string):Observable<void>{return this.http.post<void>(`${this.api}/operations/${id}/reject`,{reason},{withCredentials:true});}
 surveys():Observable<OperatorSurvey[]>{return this.http.get<OperatorSurvey[]>(`${this.api}/surveys`,{withCredentials:true});}
 start(id:number):Observable<OperatorSurvey>{return this.http.post<OperatorSurvey>(`${this.api}/surveys/${id}/start`,{},{withCredentials:true});}
 complete(id:number,body:CompleteSurvey):Observable<OperatorSurvey>{return this.http.post<OperatorSurvey>(`${this.api}/surveys/${id}/complete`,body,{withCredentials:true});}
 uploadCsv(id:number,file:File):Observable<unknown>{const form=new FormData();form.append('file',file);return this.http.post(`${this.api}/surveys/${id}/magnetic-data`,form,{withCredentials:true});}
}
