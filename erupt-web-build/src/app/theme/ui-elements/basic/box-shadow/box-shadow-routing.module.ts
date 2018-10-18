import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BoxShadowComponent} from './box-shadow.component';

const routes: Routes = [
  {
    path: '',
    component: BoxShadowComponent,
    data: {
      title: 'Box Shadow',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - box-shadow',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BoxShadowRoutingModule { }
