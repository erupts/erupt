import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormValidationComponent} from './form-validation.component';

const routes: Routes = [
  {
    path: '',
    component: FormValidationComponent,
    data: {
      title: 'Forms Validation',
      icon: 'icon-layers',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - form validation',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FormValidationRoutingModule { }
