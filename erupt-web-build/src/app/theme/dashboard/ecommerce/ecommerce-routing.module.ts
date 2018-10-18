import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {EcommerceComponent} from './ecommerce.component';

const routes: Routes = [
  {
    path: '',
    component: EcommerceComponent,
    data: {
      title: 'Ecommerce',
      icon: 'icon-home',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EcommerceRoutingModule { }
