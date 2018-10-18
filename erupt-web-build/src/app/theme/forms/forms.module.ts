 import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsRoutingModule} from './forms-routing.module';
import {SharedModule} from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    FormsRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class FormsModule { }
