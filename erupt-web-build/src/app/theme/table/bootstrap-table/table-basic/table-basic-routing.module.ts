import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TableBasicComponent} from './table-basic.component';

const routes: Routes = [
  {
    path: '',
    component: TableBasicComponent,
    data: {
      title: 'Bootstrap Basic Tables',
      icon: 'icon-receipt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - basic table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TableBasicRoutingModule { }
