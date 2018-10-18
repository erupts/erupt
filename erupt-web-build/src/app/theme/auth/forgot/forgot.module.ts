import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ForgotComponent } from './forgot.component';
import {ForgotRoutingModule} from './forgot-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    ForgotRoutingModule,
    SharedModule
  ],
  declarations: [ForgotComponent]
})
export class ForgotModule { }
