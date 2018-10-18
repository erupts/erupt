import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BorderComponent} from './border.component';

const routes: Routes = [
  {
    path: '',
    component: BorderComponent,
    data: {
      title: 'Bootstrap Border Sizes',
      icon: 'icon-receipt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - border table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BorderRoutingModule { }
