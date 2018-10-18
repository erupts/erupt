import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ChartRoutingModule} from './chart-routing.module';
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    ChartRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class ChartModule { }
