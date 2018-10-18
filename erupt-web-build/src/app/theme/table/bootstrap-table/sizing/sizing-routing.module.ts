import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SizingComponent} from './sizing.component';

const routes: Routes = [
  {
    path: '',
    component: SizingComponent,
    data: {
      title: 'Bootstrap Table Sizes',
      icon: 'icon-receipt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - sizing table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SizingRoutingModule { }
