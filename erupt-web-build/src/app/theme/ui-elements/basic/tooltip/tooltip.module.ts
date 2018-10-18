import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TooltipComponent } from './tooltip.component';
import {TooltipRoutingModule} from './tooltip-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    TooltipRoutingModule,
    SharedModule
  ],
  declarations: [TooltipComponent]
})
export class TooltipModule { }
