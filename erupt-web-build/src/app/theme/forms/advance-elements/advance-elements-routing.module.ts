import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AdvanceElementsComponent} from './advance-elements.component';

const routes: Routes = [
  {
    path: '',
    component: AdvanceElementsComponent,
    data: {
      title: 'Advance Form Inputs',
      icon: 'icon-layers',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - advance components',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdvanceElementsRoutingModule { }
