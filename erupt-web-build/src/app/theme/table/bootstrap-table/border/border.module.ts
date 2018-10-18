import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BorderComponent } from './border.component';
import {BorderRoutingModule} from './border-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BorderRoutingModule,
    SharedModule
  ],
  declarations: [BorderComponent]
})
export class BorderModule { }
