import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EcommerceComponent } from './ecommerce.component';
import {EcommerceRoutingModule} from './ecommerce-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';

@NgModule({
  imports: [
    CommonModule,
    EcommerceRoutingModule,
    SharedModule,
    ChartModule
  ],
  declarations: [EcommerceComponent]
})
export class EcommerceModule { }
