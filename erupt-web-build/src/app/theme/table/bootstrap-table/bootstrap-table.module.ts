import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {BootstrapTableRoutingModule} from './bootstrap-table-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BootstrapTableRoutingModule,
    SharedModule
  ],
  declarations: []
})
export class BootstrapTableModule { }
