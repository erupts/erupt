import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccordionComponent } from './accordion.component';
import {AccordionRoutingModule} from './accordion-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    AccordionRoutingModule,
    SharedModule
  ],
  declarations: [AccordionComponent]
})
export class AccordionModule { }
