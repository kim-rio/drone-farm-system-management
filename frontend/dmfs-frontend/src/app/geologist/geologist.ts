import { Component } from '@angular/core';
import { RoleWorkspace } from '../shared/role-workspace/role-workspace';

@Component({
  selector: 'app-geologist',
  standalone: true,
  imports: [RoleWorkspace],
  template: `
    <app-role-workspace />
  `
})
export class Geologist {}
