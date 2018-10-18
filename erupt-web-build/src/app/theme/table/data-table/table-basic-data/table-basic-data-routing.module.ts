import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TableBasicDataComponent} from './table-basic-data.component';

const routes: Routes = [
  {
    path: '',
    component: TableBasicDataComponent,
    data: {
      title: 'Basic Data Tables',
      icon: 'icon-widgetized',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - basic data table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TableBasicDataRoutingModule { }
