import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Charts',
      status: false
    },
    children: [
      {
        path: 'google',
        loadChildren: './google-chart/google-chart.module#GoogleChartModule'
      },
      {
        path: 'chart-js',
        loadChildren: './chart-js/chart-js.module#ChartJsModule'
      },
      {
        path: 'radial',
        loadChildren: './radial/radial.module#RadialModule'
      },
      {
        path: 'c3-js',
        loadChildren: './c3-js/c3-js.module#C3JsModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChartRoutingModule { }
