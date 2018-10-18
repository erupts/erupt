import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MapRoutingModule} from './map-routing.module';
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    MapRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class MapModule { }
