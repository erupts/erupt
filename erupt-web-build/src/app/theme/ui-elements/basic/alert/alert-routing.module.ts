import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AlertComponent} from './alert.component';

const routes: Routes = [
  {
    path: '',
    component: AlertComponent,
    data: {
      title: 'Alert',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - alert',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AlertRoutingModule { }
