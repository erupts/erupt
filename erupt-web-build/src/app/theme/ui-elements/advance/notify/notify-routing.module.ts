import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NotifyComponent} from './notify.component';

const routes: Routes = [
  {
    path: '',
    component: NotifyComponent,
    data: {
      title: 'Notify',
      icon: 'icon-crown',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - notify',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotifyRoutingModule { }
