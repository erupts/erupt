import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BasicInvoiceComponent } from './basic-invoice.component';
import {BasicInvoiceRoutingModule} from './basic-invoice-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BasicInvoiceRoutingModule,
    SharedModule
  ],
  declarations: [BasicInvoiceComponent]
})
export class BasicInvoiceModule { }
