import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BasicListComponent} from './basic-list.component';

const routes: Routes = [
  {
    path: '',
    component: BasicListComponent,
    data: {
      title: 'List',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - list',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BasicListRoutingModule { }
