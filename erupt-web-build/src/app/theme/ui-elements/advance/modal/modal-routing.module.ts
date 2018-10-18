import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ModalComponent} from './modal.component';

const routes: Routes = [
  {
    path: '',
    component: ModalComponent,
    data: {
      title: 'Modal',
      icon: 'icon-crown',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - modal',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ModalRoutingModule { }
