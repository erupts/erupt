import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoogleMapComponent } from './google-map.component';
import {GoogleMapRoutingModule} from './google-map-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {AgmCoreModule} from '@agm/core';

@NgModule({
  imports: [
    CommonModule,
    GoogleMapRoutingModule,
    SharedModule,
    AgmCoreModule.forRoot({apiKey: 'AIzaSyCE0nvTeHBsiQIrbpMVTe489_O5mwyqofk'})
  ],
  declarations: [GoogleMapComponent]
})
export class GoogleMapModule { }
