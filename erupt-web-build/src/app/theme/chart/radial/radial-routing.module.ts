import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RadialComponent} from './radial.component';

const routes: Routes = [
  {
    path: '',
    component: RadialComponent,
    data: {
      title: 'Radial Chart',
      icon: 'icon-bar-chart-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - radial chart',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RadialRoutingModule { }
