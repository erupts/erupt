import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BasicOtherComponent } from './basic-other.component';
import {BasicOtherRoutingModule} from './basic-other-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BasicOtherRoutingModule,
    SharedModule
  ],
  declarations: [BasicOtherComponent]
})
export class BasicOtherModule { }
