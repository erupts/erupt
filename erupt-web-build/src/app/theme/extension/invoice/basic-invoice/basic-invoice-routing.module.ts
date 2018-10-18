import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BasicInvoiceComponent} from './basic-invoice.component';

const routes: Routes = [
  {
    path: '',
    component: BasicInvoiceComponent,
    data: {
      title: 'Basic',
      icon: 'icon-layout-media-right',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - invoice basic',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BasicInvoiceRoutingModule { }
