import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PagingComponent} from './paging.component';

const routes: Routes = [
  {
    path: '',
    component: PagingComponent,
    data: {
      title: 'Paging Table',
      icon: 'icon-widgetized',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - paging table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PagingRoutingModule { }
