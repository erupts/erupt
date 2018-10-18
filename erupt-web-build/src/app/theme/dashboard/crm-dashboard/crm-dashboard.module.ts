import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrmDashboardComponent } from './crm-dashboard.component';
import {CrmDashboardRoutingModule} from './crm-dashboard-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';
import {AgmCoreModule} from '@agm/core';

@NgModule({
  imports: [
    CommonModule,
    CrmDashboardRoutingModule,
    SharedModule,
    ChartModule,
    AgmCoreModule.forRoot({apiKey: 'AIzaSyCE0nvTeHBsiQIrbpMVTe489_O5mwyqofk'})
  ],
  declarations: [CrmDashboardComponent]
})
export class CrmDashboardModule { }
