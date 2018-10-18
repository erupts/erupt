import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BasicListComponent } from './basic-list.component';
import {BasicListRoutingModule} from './basic-list-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BasicListRoutingModule,
    SharedModule
  ],
  declarations: [BasicListComponent]
})
export class BasicListModule { }
