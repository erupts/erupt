import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ChartJsComponent} from './chart-js.component';

const routes: Routes = [
  {
    path: '',
    component: ChartJsComponent,
    data: {
      title: 'ChartJs',
      icon: 'icon-bar-chart-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - chart js',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChartJsRoutingModule { }
