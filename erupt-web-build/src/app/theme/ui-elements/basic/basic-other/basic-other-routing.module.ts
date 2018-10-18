import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BasicOtherComponent} from './basic-other.component';

const routes: Routes = [
  {
    path: '',
    component: BasicOtherComponent,
    data: {
      title: 'Other',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - other',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BasicOtherRoutingModule { }
