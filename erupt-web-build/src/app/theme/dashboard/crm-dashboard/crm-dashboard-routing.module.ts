import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {CrmDashboardComponent} from './crm-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: CrmDashboardComponent,
    data: {
      title: 'CRM Dashboard',
      icon: 'icon-home',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CrmDashboardRoutingModule { }
