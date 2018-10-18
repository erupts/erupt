import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {GoogleChartComponent} from './google-chart.component';

const routes: Routes = [
  {
    path: '',
    component: GoogleChartComponent,
    data: {
      title: 'Google Charts',
      icon: 'icon-bar-chart-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - google chart',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GoogleChartRoutingModule { }
