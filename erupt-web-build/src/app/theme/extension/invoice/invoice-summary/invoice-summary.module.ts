import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InvoiceSummaryComponent } from './invoice-summary.component';
import {InvoiceSummaryRoutingModule} from './invoice-summary-routing.module';
import {SharedModule} from '../../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';

@NgModule({
  imports: [
    CommonModule,
    InvoiceSummaryRoutingModule,
    SharedModule,
    ChartModule
  ],
  declarations: [InvoiceSummaryComponent]
})
export class InvoiceSummaryModule { }
