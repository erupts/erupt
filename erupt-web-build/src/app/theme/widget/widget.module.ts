import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {SharedModule} from '../../shared/shared.module';
import {WidgetRoutingModule} from './widget-routing.module';

@NgModule({
  imports: [
    CommonModule,
    WidgetRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class WidgetModule { }
