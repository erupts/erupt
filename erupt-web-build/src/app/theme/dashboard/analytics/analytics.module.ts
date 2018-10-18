import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsComponent } from './analytics.component';
import {AnalyticsRoutingModule} from './analytics-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';

@NgModule({
  imports: [
    CommonModule,
    AnalyticsRoutingModule,
    SharedModule,
    ChartModule
  ],
  declarations: [AnalyticsComponent]
})
export class AnalyticsModule { }
