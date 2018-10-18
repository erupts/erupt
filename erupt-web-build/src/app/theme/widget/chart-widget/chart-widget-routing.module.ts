import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ChartWidgetComponent} from './chart-widget.component';

const routes: Routes = [
  {
    path: '',
    component: ChartWidgetComponent,
    data: {
      title: 'Widget Chart',
      icon: 'icon-view-grid',
      caption: 'More than 100+ widget',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChartWidgetRoutingModule { }
