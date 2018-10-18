import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {StylingComponent} from './styling.component';

const routes: Routes = [
  {
    path: '',
    component: StylingComponent,
    data: {
      title: 'Bootstrap Styling Tables',
      icon: 'icon-receipt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - styling table',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StylingRoutingModule { }
