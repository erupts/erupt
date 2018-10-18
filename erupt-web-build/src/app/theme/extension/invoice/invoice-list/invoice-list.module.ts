import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InvoiceListComponent } from './invoice-list.component';
import {InvoiceListRoutingModule} from './invoice-list-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    InvoiceListRoutingModule,
    SharedModule
  ],
  declarations: [InvoiceListComponent]
})
export class InvoiceListModule { }
