import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BasicElementsComponent} from './basic-elements.component';

const routes: Routes = [
  {
    path: '',
    component: BasicElementsComponent,
    data: {
      title: 'Basic Form Inputs',
      icon: 'icon-layers',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - form components',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BasicElementsRoutingModule { }
