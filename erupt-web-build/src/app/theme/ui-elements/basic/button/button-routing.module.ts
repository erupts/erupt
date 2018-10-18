import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ButtonComponent} from './button.component';

const routes: Routes = [
  {
    path: '',
    component: ButtonComponent,
    data: {
      title: 'Buttons',
      icon: 'icon-layout-grid2-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - button',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ButtonRoutingModule { }
