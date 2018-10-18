import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartJsComponent } from './chart-js.component';
import {ChartJsRoutingModule} from './chart-js-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';

@NgModule({
  imports: [
    CommonModule,
    ChartJsRoutingModule,
    SharedModule,
    ChartModule
  ],
  declarations: [ChartJsComponent]
})
export class ChartJsModule { }
