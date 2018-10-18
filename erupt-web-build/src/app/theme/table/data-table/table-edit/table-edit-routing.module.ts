import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TableEditComponent} from './table-edit.component';

const routes: Routes = [
  {
    path: '',
    component: TableEditComponent,
    data: {
      title: 'Editable Data Table',
      icon: 'icon-widgetized',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - editable',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TableEditRoutingModule { }
