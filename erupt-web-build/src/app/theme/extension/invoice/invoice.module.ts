import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {InvoiceRoutingModule} from './invoice-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    InvoiceRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class InvoiceModule { }
