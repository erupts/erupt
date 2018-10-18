import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoogleChartComponent } from './google-chart.component';
import {GoogleChartRoutingModule} from './google-chart-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {Ng2GoogleChartsModule} from 'ng2-google-charts';

@NgModule({
  imports: [
    CommonModule,
    GoogleChartRoutingModule,
    SharedModule,
    Ng2GoogleChartsModule
  ],
  declarations: [GoogleChartComponent]
})
export class GoogleChartModule { }
