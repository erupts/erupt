import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SelectionComponent} from './selection.component';

const routes: Routes = [
  {
    path: '',
    component: SelectionComponent,
    data: {
      title: 'Select Table',
      icon: 'icon-widgetized',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - select table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SelectionRoutingModule { }
