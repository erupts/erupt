import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ErrorComponent} from './error.component';

const routes: Routes = [
  {
    path: '',
    component: ErrorComponent,
    data: {
      title: 'Error pages',
      icon: 'icon-settings',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - error',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ErrorRoutingModule { }
