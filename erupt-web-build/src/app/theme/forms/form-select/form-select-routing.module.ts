import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormSelectComponent} from './form-select.component';

const routes: Routes = [
  {
    path: '',
    component: FormSelectComponent,
    data: {
      title: 'Form Select',
      icon: 'icon-shortcode',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - form select',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FormSelectRoutingModule { }
