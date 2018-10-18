import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BasicColorComponent} from './basic-color.component';

const routes: Routes = [
  {
    path: '',
    component: BasicColorComponent,
    data: {
      title: 'Color',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - color',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BasicColorRoutingModule { }
