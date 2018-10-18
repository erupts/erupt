import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AddOnComponent} from './add-on.component';

const routes: Routes = [
  {
    path: '',
    component: AddOnComponent,
    data: {
      title: 'Group Add-ons',
      icon: 'icon-layers',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - add-ons',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AddOnRoutingModule { }
