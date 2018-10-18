import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdvanceWidgetComponent } from './advance-widget.component';
import {AdvanceWidgetRoutingModule} from './advance-widget-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    AdvanceWidgetRoutingModule,
    SharedModule
  ],
  declarations: [AdvanceWidgetComponent]
})
export class AdvanceWidgetModule { }
