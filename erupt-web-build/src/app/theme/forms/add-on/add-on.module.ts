import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddOnComponent } from './add-on.component';
import {AddOnRoutingModule} from './add-on-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    AddOnRoutingModule,
    SharedModule
  ],
  declarations: [AddOnComponent]
})
export class AddOnModule { }
