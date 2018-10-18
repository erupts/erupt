import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FormPickerComponent} from './form-picker.component';

const routes: Routes = [
  {
    path: '',
    component: FormPickerComponent,
    data: {
      title: 'Form Picker',
      icon: 'icon-pencil-alt',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - form picker',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FormPickerRoutingModule { }
