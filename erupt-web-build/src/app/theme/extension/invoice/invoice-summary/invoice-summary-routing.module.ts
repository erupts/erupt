import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {InvoiceSummaryComponent} from './invoice-summary.component';

const routes: Routes = [
  {
    path: '',
    component: InvoiceSummaryComponent,
    data: {
      title: 'Invoice Summary',
      icon: 'icon-layout-media-right',
      caption: 'lorem ipsum dolor sit amet, consectetur adipisicing elit - invoice summary',
      status: true
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvoiceSummaryRoutingModule { }
