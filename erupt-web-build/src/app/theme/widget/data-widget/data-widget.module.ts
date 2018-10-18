import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DataWidgetComponent, AppToDoListReadDirective, AppToDoListRemoveDirective} from './data-widget.component';
import {DataWidgetRoutingModule} from './data-widget-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {ChartModule} from 'angular2-chartjs';
import {AgmCoreModule} from '@agm/core';

@NgModule({
  imports: [
    CommonModule,
    DataWidgetRoutingModule,
    SharedModule,
    ChartModule,
    AgmCoreModule.forRoot({apiKey: 'AIzaSyCE0nvTeHBsiQIrbpMVTe489_O5mwyqofk'})
  ],
  declarations: [
    AppToDoListReadDirective,
    AppToDoListRemoveDirective,
    DataWidgetComponent
  ]
})
export class DataWidgetModule { }
