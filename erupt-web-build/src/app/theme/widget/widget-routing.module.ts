import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Widget',
      status: false
    },
    children: [
      {
        path: 'statistic',
        loadChildren: './statistic/statistic.module#StatisticModule'
      },
      {
        path: 'data',
        loadChildren: './data-widget/data-widget.module#DataWidgetModule'
      },
      {
        path: 'chart',
        loadChildren: './chart-widget/chart-widget.module#ChartWidgetModule'
      },
      {
        path: 'advanced',
        loadChildren: './advance-widget/advance-widget.module#AdvanceWidgetModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class WidgetRoutingModule { }
