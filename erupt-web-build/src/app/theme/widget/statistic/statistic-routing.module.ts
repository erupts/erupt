import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {StatisticComponent} from './statistic.component';

const routes: Routes = [
  {
    path: '',
    component: StatisticComponent,
    data: {
      title: 'Widget Statistics',
      icon: 'icon-view-grid',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StatisticRoutingModule { }
