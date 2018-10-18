import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BasicElementsComponent } from './basic-elements.component';
import {BasicElementsRoutingModule} from './basic-elements-routing.module';
import {SharedModule} from '../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    BasicElementsRoutingModule,
    SharedModule
  ],
  declarations: [BasicElementsComponent]
})
export class BasicElementsModule { }
