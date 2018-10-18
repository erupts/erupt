import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableBasicComponent } from './table-basic.component';
import {TableBasicRoutingModule} from './table-basic-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    TableBasicRoutingModule,
    SharedModule
  ],
  declarations: [TableBasicComponent]
})
export class TableBasicModule { }
