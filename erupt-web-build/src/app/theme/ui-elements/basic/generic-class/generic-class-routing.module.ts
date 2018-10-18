import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {GenericClassComponent} from './generic-class.component';

const routes: Routes = [
  {
    path: '',
    component: GenericClassComponent,
    data: {
      title: 'Generic Class',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - generic class',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GenericClassRoutingModule { }
