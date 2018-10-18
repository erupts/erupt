import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BasicColorComponent } from './basic-color.component';
import {BasicColorRoutingModule} from './basic-color-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BasicColorRoutingModule,
    SharedModule
  ],
  declarations: [BasicColorComponent]
})
export class BasicColorModule { }
