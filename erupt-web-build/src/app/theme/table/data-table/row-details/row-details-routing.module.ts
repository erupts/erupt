import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RowDetailsComponent} from './row-details.component';

const routes: Routes = [
  {
    path: '',
    component: RowDetailsComponent,
    data: {
      title: 'Extra Information Details',
      icon: 'icon-widgetized',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - row detsils',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RowDetailsRoutingModule { }
