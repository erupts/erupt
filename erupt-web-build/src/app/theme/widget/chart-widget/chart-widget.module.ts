import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartWidgetComponent } from './chart-widget.component';
import {ChartWidgetRoutingModule} from './chart-widget-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';

@NgModule({
  imports: [
    CommonModule,
    ChartWidgetRoutingModule,
    SharedModule,
    ChartModule
  ],
  declarations: [ChartWidgetComponent]
})
export class ChartWidgetModule { }
