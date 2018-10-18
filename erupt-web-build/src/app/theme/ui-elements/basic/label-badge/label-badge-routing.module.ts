import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LabelBadgeComponent} from './label-badge.component';

const routes: Routes = [
  {
    path: '',
    component: LabelBadgeComponent,
    data: {
      title: 'Label Badge',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - label badge',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LabelBadgeRoutingModule { }
