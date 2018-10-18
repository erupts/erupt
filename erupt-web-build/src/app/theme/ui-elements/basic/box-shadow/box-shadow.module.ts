import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoxShadowComponent } from './box-shadow.component';
import {BoxShadowRoutingModule} from './box-shadow-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BoxShadowRoutingModule,
    SharedModule
  ],
  declarations: [BoxShadowComponent]
})
export class BoxShadowModule { }
