import { NgModule } from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Invoice',
      status: false
    },
    children: [
      {
        path: 'basic',
        loadChildren: './basic-invoice/basic-invoice.module#BasicInvoiceModule'
      },
      {
        path: 'summary',
        loadChildren: './invoice-summary/invoice-summary.module#InvoiceSummaryModule'
      },
      {
        path: 'list',
        loadChildren: './invoice-list/invoice-list.module#InvoiceListModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvoiceRoutingModule { }
